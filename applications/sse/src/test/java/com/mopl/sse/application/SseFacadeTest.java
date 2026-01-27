package com.mopl.sse.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseFacade 단위 테스트")
class SseFacadeTest {

    @Mock
    private SseEmitterManager sseEmitterManager;

    @InjectMocks
    private SseFacade sseFacade;

    @Nested
    @DisplayName("subscribe()")
    class SubscribeTest {

        @Test
        @DisplayName("구독 성공 시 emitter 반환 및 연결 이벤트 전송")
        void withValidUserId_returnsEmitterAndSendsConnectEvent() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            UUID eventId = UUID.randomUUID();

            given(sseEmitterManager.createEmitter(userId)).willReturn(emitter);
            given(sseEmitterManager.generateEventId()).willReturn(eventId);

            // when
            SseEmitter result = sseFacade.subscribe(userId, null);

            // then
            assertThat(result).isEqualTo(emitter);
            then(sseEmitterManager).should().createEmitter(userId);
            then(emitter).should().send(any(SseEmitter.SseEventBuilder.class));
        }

        @Test
        @DisplayName("lastEventId가 있으면 이전 이벤트 재전송")
        void withLastEventId_resendsEvents() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            UUID eventId = UUID.randomUUID();

            given(sseEmitterManager.createEmitter(userId)).willReturn(emitter);
            given(sseEmitterManager.generateEventId()).willReturn(eventId);

            // when
            SseEmitter result = sseFacade.subscribe(userId, lastEventId);

            // then
            assertThat(result).isEqualTo(emitter);
            then(sseEmitterManager).should().resendEventsAfter(userId, lastEventId, emitter);
        }

        @Test
        @DisplayName("lastEventId가 null이면 이전 이벤트 재전송 안함")
        void withoutLastEventId_doesNotResendEvents() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            UUID eventId = UUID.randomUUID();

            given(sseEmitterManager.createEmitter(userId)).willReturn(emitter);
            given(sseEmitterManager.generateEventId()).willReturn(eventId);

            // when
            SseEmitter result = sseFacade.subscribe(userId, null);

            // then
            assertThat(result).isEqualTo(emitter);
            then(sseEmitterManager).should(never())
                .resendEventsAfter(any(), any(), any());
        }

        @Test
        @DisplayName("연결 이벤트 전송 실패 시 에러로 완료")
        void whenConnectEventFails_completesWithError() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = mock(SseEmitter.class);
            UUID eventId = UUID.randomUUID();
            IOException ioException = new IOException("Send failed");

            given(sseEmitterManager.createEmitter(userId)).willReturn(emitter);
            given(sseEmitterManager.generateEventId()).willReturn(eventId);
            willThrow(ioException).given(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // when
            SseEmitter result = sseFacade.subscribe(userId, null);

            // then
            assertThat(result).isEqualTo(emitter);
            then(emitter).should().completeWithError(ioException);
            then(sseEmitterManager).should(never())
                .resendEventsAfter(any(), any(), any());
        }
    }
}
