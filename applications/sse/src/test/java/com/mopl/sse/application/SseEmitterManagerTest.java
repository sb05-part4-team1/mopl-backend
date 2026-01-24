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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
    }

    @Nested
    @DisplayName("sendHeartbeat()")
    class SendHeartbeatTest {

        @Test
        @DisplayName("모든 로컬 emitter에 heartbeat 전송")
        void sendsHeartbeatToAllEmitters() throws IOException, InterruptedException {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
            emitters.put(userId, emitter);

            given(emitterRepository.getLocalEmitters()).willReturn(emitters);

            // when
            sseEmitterManager.sendHeartbeat();

            // Virtual thread executor로 heartbeat 전송 완료 대기
            Thread.sleep(100);

            // then
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }
    }
}
