package com.mopl.sse.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.dto.conversation.DirectMessageResponse;
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
@DisplayName("RedisDirectMessageSubscriber 단위 테스트")
class RedisDirectMessageSubscriberTest {

    @Mock
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Mock
    private SseEmitterManager sseEmitterManager;

    private RedisDirectMessageSubscriber redisDirectMessageSubscriber;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        redisDirectMessageSubscriber = new RedisDirectMessageSubscriber(
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
            redisDirectMessageSubscriber.subscribe();

            // then
            then(redisMessageListenerContainer).should()
                .addMessageListener(eq(redisDirectMessageSubscriber), any(ChannelTopic.class));
        }
    }

    @Nested
    @DisplayName("onMessage()")
    class OnMessageTest {

        @Test
        @DisplayName("로컬 emitter가 있으면 SSE 이벤트 전송")
        void withLocalEmitter_sendsDirectMessage() {
            // given
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            UUID messageId = UUID.randomUUID();

            String json = """
                {
                    "id": "%s",
                    "conversationId": "%s",
                    "createdAt": "2024-01-01T10:00:00Z",
                    "sender": {
                        "userId": "%s",
                        "name": "Sender",
                        "profileImageUrl": null
                    },
                    "receiver": {
                        "userId": "%s",
                        "name": "Receiver",
                        "profileImageUrl": null
                    },
                    "content": "Hello"
                }
                """.formatted(messageId, conversationId, senderId, receiverId);

            Message message = new DefaultMessage("sse:direct-messages".getBytes(), json.getBytes());

            given(sseEmitterManager.hasLocalEmitter(receiverId)).willReturn(true);

            // when
            redisDirectMessageSubscriber.onMessage(message, null);

            // then
            then(sseEmitterManager).should().hasLocalEmitter(receiverId);
            then(sseEmitterManager).should()
                .sendToUser(eq(receiverId), eq("direct-messages"), any(DirectMessageResponse.class));
        }

        @Test
        @DisplayName("로컬 emitter가 없으면 이벤트 전송 안함")
        void withoutLocalEmitter_doesNotSendDirectMessage() {
            // given
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            UUID messageId = UUID.randomUUID();

            String json = """
                {
                    "id": "%s",
                    "conversationId": "%s",
                    "createdAt": "2024-01-01T10:00:00Z",
                    "sender": {
                        "userId": "%s",
                        "name": "Sender",
                        "profileImageUrl": null
                    },
                    "receiver": {
                        "userId": "%s",
                        "name": "Receiver",
                        "profileImageUrl": null
                    },
                    "content": "Hello"
                }
                """.formatted(messageId, conversationId, senderId, receiverId);

            Message message = new DefaultMessage("sse:direct-messages".getBytes(), json.getBytes());

            given(sseEmitterManager.hasLocalEmitter(receiverId)).willReturn(false);

            // when
            redisDirectMessageSubscriber.onMessage(message, null);

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
            Message message = new DefaultMessage("sse:direct-messages".getBytes(), invalidBody);

            // when
            redisDirectMessageSubscriber.onMessage(message, null);

            // then
            then(sseEmitterManager).should(never()).hasLocalEmitter(any());
            then(sseEmitterManager).should(never()).sendToUser(any(), any(), any());
        }
    }
}
