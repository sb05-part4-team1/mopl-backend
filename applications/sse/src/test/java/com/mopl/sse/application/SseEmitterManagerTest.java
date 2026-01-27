package com.mopl.sse.application;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.sse.repository.RedisEmitterRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseEmitterManager 단위 테스트")
class SseEmitterManagerTest {

    @Mock
    private RedisEmitterRepository emitterRepository;

    @Mock
    private NotificationQueryRepository notificationQueryRepository;

    private SseEmitterManager sseEmitterManager;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        sseEmitterManager = new SseEmitterManager(
            emitterRepository,
            notificationQueryRepository,
            meterRegistry
        );
        sseEmitterManager.initMetrics();
    }

    @Nested
    @DisplayName("createEmitter()")
    class CreateEmitterTest {

        @Test
        @DisplayName("새 emitter 생성 및 저장")
        void createsAndSavesEmitter() {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when
            SseEmitter result = sseEmitterManager.createEmitter(userId);

            // then
            assertThat(result).isNotNull();
            then(emitterRepository).should().save(eq(userId), any(SseEmitter.class));
        }

        @Test
        @DisplayName("기존 emitter가 있으면 완료 후 새로 생성")
        void withExistingEmitter_completesAndCreatesNew() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter existingEmitter = mock(SseEmitter.class);
            given(emitterRepository.findByUserId(userId)).willReturn(Optional.of(existingEmitter));

            // when
            SseEmitter result = sseEmitterManager.createEmitter(userId);

            // then
            assertThat(result).isNotNull();
            then(existingEmitter).should().complete();
            then(emitterRepository).should().save(eq(userId), any(SseEmitter.class));
        }

        @Test
        @DisplayName("emitter onCompletion 콜백이 호출되면 repository에서 삭제")
        void onCompletion_deletesFromRepository() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.findByUserId(userId)).willReturn(Optional.empty());
            SseEmitter emitter = sseEmitterManager.createEmitter(userId);

            // when - 리플렉션으로 onCompletion 콜백 직접 실행
            Field field = SseEmitter.class.getSuperclass().getDeclaredField("completionCallback");
            field.setAccessible(true);
            Runnable callback = (Runnable) field.get(emitter);
            callback.run();

            // then
            then(emitterRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("emitter onTimeout 콜백이 호출되면 repository에서 삭제")
        void onTimeout_deletesFromRepository() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.findByUserId(userId)).willReturn(Optional.empty());
            SseEmitter emitter = sseEmitterManager.createEmitter(userId);

            // when - 리플렉션으로 onTimeout 콜백 직접 실행
            Field field = SseEmitter.class.getSuperclass().getDeclaredField("timeoutCallback");
            field.setAccessible(true);
            Runnable callback = (Runnable) field.get(emitter);
            callback.run();

            // then
            then(emitterRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("emitter onError 콜백이 호출되면 repository에서 삭제")
        @SuppressWarnings("unchecked")
        void onError_deletesFromRepository() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.findByUserId(userId)).willReturn(Optional.empty());
            SseEmitter emitter = sseEmitterManager.createEmitter(userId);

            // when - 리플렉션으로 onError 콜백 직접 실행
            Field field = SseEmitter.class.getSuperclass().getDeclaredField("errorCallback");
            field.setAccessible(true);
            Consumer<Throwable> callback = (Consumer<Throwable>) field.get(emitter);
            callback.accept(new IOException("Connection reset"));

            // then
            then(emitterRepository).should().deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("sendToUser()")
    class SendToUserTest {

        @Test
        @DisplayName("사용자에게 이벤트 전송 성공")
        void sendsEventToUser() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            String eventName = "notifications";
            Object data = "test data";
            SseEmitter emitter = mock(SseEmitter.class);

            given(emitterRepository.findByUserId(userId)).willReturn(Optional.of(emitter));

            // when
            sseEmitterManager.sendToUser(userId, eventName, data);

            // then
            then(emitterRepository).should().cacheEvent(eq(userId), any(UUID.class), eq(data));
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("emitter가 없으면 이벤트만 캐싱")
        void withoutEmitter_onlyCachesEvent() {
            // given
            UUID userId = UUID.randomUUID();
            String eventName = "notifications";
            Object data = "test data";

            given(emitterRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when
            sseEmitterManager.sendToUser(userId, eventName, data);

            // then
            then(emitterRepository).should().cacheEvent(eq(userId), any(UUID.class), eq(data));
        }

        @Test
        @DisplayName("전송 실패 시 emitter 삭제")
        void withSendFailure_deletesEmitter() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            String eventName = "notifications";
            Object data = "test data";
            SseEmitter emitter = mock(SseEmitter.class);

            given(emitterRepository.findByUserId(userId)).willReturn(Optional.of(emitter));
            doThrow(new IOException("Connection closed"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // when
            sseEmitterManager.sendToUser(userId, eventName, data);

            // then
            then(emitterRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("전송 실패 후 complete에서도 예외 발생하면 조용히 무시")
        void withSendAndCompleteFailure_ignoresCompleteException() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            String eventName = "notifications";
            Object data = "test data";
            SseEmitter emitter = mock(SseEmitter.class);

            given(emitterRepository.findByUserId(userId)).willReturn(Optional.of(emitter));
            doThrow(new IOException("Connection closed"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));
            doThrow(new IllegalStateException("Already completed"))
                .when(emitter).complete();

            // when - 예외 없이 완료되어야 함
            sseEmitterManager.sendToUser(userId, eventName, data);

            // then
            then(emitter).should().complete();
            then(emitterRepository).should().deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("hasLocalEmitter()")
    class HasLocalEmitterTest {

        @Test
        @DisplayName("로컬 emitter 존재 여부 확인")
        void checksLocalEmitterExists() {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.existsLocally(userId)).willReturn(true);

            // when
            boolean result = sseEmitterManager.hasLocalEmitter(userId);

            // then
            assertThat(result).isTrue();
            then(emitterRepository).should().existsLocally(userId);
        }
    }

    @Nested
    @DisplayName("generateEventId()")
    class GenerateEventIdTest {

        @Test
        @DisplayName("UUID v7 이벤트 ID 생성")
        void generatesUuidV7EventId() {
            // when
            UUID eventId = sseEmitterManager.generateEventId();

            // then
            assertThat(eventId).isNotNull();
            int version = (int) ((eventId.getMostSignificantBits() >> 12) & 0xF);
            assertThat(version).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("resendEventsAfter()")
    class ResendEventsAfterTest {

        @Test
        @DisplayName("캐시된 이벤트가 있으면 캐시에서 재전송")
        void withCachedEvents_resendsFromCache() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            SseEmitter emitter = mock(SseEmitter.class);

            UUID cachedEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdf0");
            RedisEmitterRepository.CachedEvent cachedEvent = new RedisEmitterRepository.CachedEvent(cachedEventId, "cached data");

            given(emitterRepository.getEventsAfter(userId, lastEventId))
                .willReturn(List.of(cachedEvent));

            // when
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);

            // then
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
            then(notificationQueryRepository).should(never())
                .findByReceiverIdAndCreatedAtAfter(any(), any());
        }

        @Test
        @DisplayName("캐시된 이벤트가 없으면 DB에서 조회하여 재전송")
        void withoutCachedEvents_resendsFromDb() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            SseEmitter emitter = mock(SseEmitter.class);

            NotificationModel notification = NotificationModel.create(
                "테스트 알림",
                "내용",
                NotificationModel.NotificationLevel.INFO,
                userId
            );

            given(emitterRepository.getEventsAfter(userId, lastEventId))
                .willReturn(List.of());
            given(notificationQueryRepository.findByReceiverIdAndCreatedAtAfter(
                eq(userId), any(Instant.class)))
                .willReturn(List.of(notification));

            // when
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);

            // then
            then(notificationQueryRepository).should()
                .findByReceiverIdAndCreatedAtAfter(eq(userId), any(Instant.class));
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("캐시에서 재전송 시 IOException 발생하면 중단")
        void withCacheResendFailure_stopsResending() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            SseEmitter emitter = mock(SseEmitter.class);

            UUID cachedEventId1 = UUID.fromString("01934567-89ab-7def-0123-456789abcdf0");
            UUID cachedEventId2 = UUID.fromString("01934567-89ab-7def-0123-456789abcdf1");
            RedisEmitterRepository.CachedEvent cachedEvent1 = new RedisEmitterRepository.CachedEvent(cachedEventId1, "data1");
            RedisEmitterRepository.CachedEvent cachedEvent2 = new RedisEmitterRepository.CachedEvent(cachedEventId2, "data2");

            given(emitterRepository.getEventsAfter(userId, lastEventId))
                .willReturn(List.of(cachedEvent1, cachedEvent2));
            doThrow(new IOException("Connection closed"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // when
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);

            // then - send는 첫 번째 호출에서 실패하므로 한 번만 호출됨
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("DB에서 재전송 시 IOException 발생하면 중단")
        void withDbResendFailure_stopsResending() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            SseEmitter emitter = mock(SseEmitter.class);

            NotificationModel notification1 = NotificationModel.create(
                "알림1", "내용1", NotificationModel.NotificationLevel.INFO, userId);
            NotificationModel notification2 = NotificationModel.create(
                "알림2", "내용2", NotificationModel.NotificationLevel.INFO, userId);

            given(emitterRepository.getEventsAfter(userId, lastEventId))
                .willReturn(List.of());
            given(notificationQueryRepository.findByReceiverIdAndCreatedAtAfter(
                eq(userId), any(Instant.class)))
                .willReturn(List.of(notification1, notification2));
            doThrow(new IOException("Connection closed"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // when
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);

            // then - send는 첫 번째 호출에서 실패하므로 한 번만 호출됨
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("캐시와 DB 모두 비어있으면 아무것도 전송하지 않음")
        void withEmptyCacheAndDb_sendsNothing() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            SseEmitter emitter = mock(SseEmitter.class);

            given(emitterRepository.getEventsAfter(userId, lastEventId))
                .willReturn(List.of());
            given(notificationQueryRepository.findByReceiverIdAndCreatedAtAfter(
                eq(userId), any(Instant.class)))
                .willReturn(List.of());

            // when
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);

            // then
            then(emitter).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("sendHeartbeat()")
    class SendHeartbeatTest {

        @Test
        @DisplayName("모든 로컬 emitter에 heartbeat 전송")
        void sendsHeartbeatToAllEmitters() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
            emitters.put(userId, emitter);

            given(emitterRepository.getLocalEmitters()).willReturn(emitters);

            // when
            sseEmitterManager.sendHeartbeat();

            // then - timeout으로 비동기 완료 대기
            then(emitter).should(timeout(1000)).send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("heartbeat 전송 실패 시 emitter 삭제")
        void withHeartbeatFailure_deletesEmitter() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
            emitters.put(userId, emitter);

            given(emitterRepository.getLocalEmitters()).willReturn(emitters);
            doThrow(new IOException("Connection closed"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // when
            sseEmitterManager.sendHeartbeat();

            // then - timeout으로 비동기 완료 대기
            then(emitterRepository).should(timeout(1000)).deleteByUserId(userId);
        }

        @Test
        @DisplayName("emitter가 없으면 아무것도 전송하지 않음")
        void withNoEmitters_sendsNothing() {
            // given
            given(emitterRepository.getLocalEmitters()).willReturn(new ConcurrentHashMap<>());

            // when
            sseEmitterManager.sendHeartbeat();

            // then
            then(emitterRepository).should(never()).deleteByUserId(any());
        }
    }

    @Nested
    @DisplayName("hasLocalEmitter() false 케이스")
    class HasLocalEmitterFalseTest {

        @Test
        @DisplayName("로컬 emitter가 없으면 false 반환")
        void withNoLocalEmitter_returnsFalse() {
            // given
            UUID userId = UUID.randomUUID();
            given(emitterRepository.existsLocally(userId)).willReturn(false);

            // when
            boolean result = sseEmitterManager.hasLocalEmitter(userId);

            // then
            assertThat(result).isFalse();
        }
    }
}
