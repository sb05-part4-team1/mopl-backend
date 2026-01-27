package com.mopl.sse.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.sse.application.SseEmitterManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisNotificationSubscriber 단위 테스트")
class RedisNotificationSubscriberTest {

    @Mock
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Mock
    private SseEmitterManager sseEmitterManager;

    private RedisNotificationSubscriber redisNotificationSubscriber;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        redisNotificationSubscriber = new RedisNotificationSubscriber(
            redisMessageListenerContainer,
            sseEmitterManager,
            objectMapper
        );
    }

    @Nested
    @DisplayName("subscribe()")
    class SubscribeTest {

        @Test
        @DisplayName("Redis 채널 구독 등록")
        void registersMessageListener() {
            // when
            redisNotificationSubscriber.subscribe();

            // then
            then(redisMessageListenerContainer).should()
                .addMessageListener(eq(redisNotificationSubscriber), any(ChannelTopic.class));
        }
    }

    @Nested
    @DisplayName("onMessage()")
    class OnMessageTest {

        @Test
        @DisplayName("로컬 emitter가 있으면 SSE 이벤트 전송")
        void withLocalEmitter_sendsNotification() throws Exception {
            // given
            UUID receiverId = UUID.randomUUID();
            String json = """
                {
                    "title": "테스트 알림",
                    "content": "내용",
                    "level": "INFO",
                    "receiverId": "%s"
                }
                """.formatted(receiverId);

            Message message = new DefaultMessage("sse:notifications".getBytes(), json.getBytes());

            given(sseEmitterManager.hasLocalEmitter(receiverId)).willReturn(true);

            // when
            redisNotificationSubscriber.onMessage(message, null);

            // then
            then(sseEmitterManager).should().hasLocalEmitter(receiverId);
            then(sseEmitterManager).should()
                .sendToUser(eq(receiverId), eq("notifications"), any(NotificationModel.class));
        }

        @Test
        @DisplayName("로컬 emitter가 없으면 이벤트 전송 안함")
        void withoutLocalEmitter_doesNotSendNotification() throws Exception {
            // given
            UUID receiverId = UUID.randomUUID();
            String json = """
                {
                    "title": "테스트 알림",
                    "content": "내용",
                    "level": "INFO",
                    "receiverId": "%s"
                }
                """.formatted(receiverId);

            Message message = new DefaultMessage("sse:notifications".getBytes(), json.getBytes());

            given(sseEmitterManager.hasLocalEmitter(receiverId)).willReturn(false);

            // when
            redisNotificationSubscriber.onMessage(message, null);

            // then
            then(sseEmitterManager).should().hasLocalEmitter(receiverId);
            then(sseEmitterManager).should(never())
                .sendToUser(any(), any(), any());
        }

        @Test
        @DisplayName("잘못된 메시지 형식이면 예외 처리하고 계속 진행")
        void withInvalidMessage_handlesException() {
            // given
            byte[] invalidBody = "invalid json".getBytes();
            Message message = new DefaultMessage("sse:notifications".getBytes(), invalidBody);

            // when
            redisNotificationSubscriber.onMessage(message, null);

            // then
            then(sseEmitterManager).should(never()).hasLocalEmitter(any());
            then(sseEmitterManager).should(never()).sendToUser(any(), any(), any());
        }
    }
}
