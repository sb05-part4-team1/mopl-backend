package com.mopl.websocket.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisWebSocketMessageSubscriber 단위 테스트")
class RedisWebSocketMessageSubscriberTest {

    @Mock
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private RedisWebSocketMessageSubscriber subscriber;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        subscriber = new RedisWebSocketMessageSubscriber(
            redisMessageListenerContainer,
            messagingTemplate,
            objectMapper
        );
    }

    @Nested
    @DisplayName("onMessage()")
    class OnMessageTest {

        @Test
        @DisplayName("유효한 메시지 수신 시 WebSocket으로 전달")
        void withValidMessage_forwardsToWebSocket() {
            // given
            String destination = "/sub/contents/123/watch";
            String jsonMessage = """
                {
                    "destination": "%s",
                    "payload": {
                        "type": "USER_JOINED",
                        "userId": "user-123",
                        "username": "테스트유저"
                    }
                }
                """.formatted(destination);

            Message message = createRedisMessage(jsonMessage);

            // when
            subscriber.onMessage(message, null);

            // then
            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
            then(messagingTemplate).should().convertAndSend(eq(destination), payloadCaptor.capture());

            Object capturedPayload = payloadCaptor.getValue();
            assertThat(capturedPayload).isNotNull();
        }

        @Test
        @DisplayName("복잡한 payload 메시지 정상 처리")
        void withComplexPayload_forwardsCorrectly() {
            // given
            String destination = "/sub/contents/456/chat";
            String jsonMessage = """
                {
                    "destination": "%s",
                    "payload": {
                        "messageId": "msg-001",
                        "senderId": "user-001",
                        "senderName": "홍길동",
                        "content": "안녕하세요!",
                        "timestamp": "2024-01-15T10:30:00Z",
                        "metadata": {
                            "type": "text",
                            "reactions": []
                        }
                    }
                }
                """.formatted(destination);

            Message message = createRedisMessage(jsonMessage);

            // when
            subscriber.onMessage(message, null);

            // then
            then(messagingTemplate).should().convertAndSend(eq(destination), any(Object.class));
        }

        @Test
        @DisplayName("잘못된 JSON 메시지 수신 시 예외 처리하고 전달하지 않음")
        void withInvalidJson_doesNotForward() {
            // given
            String invalidJson = "{ invalid json }";
            Message message = createRedisMessage(invalidJson);

            // when
            subscriber.onMessage(message, null);

            // then
            then(messagingTemplate).should(never()).convertAndSend(any(String.class), any(Object.class));
        }

        @Test
        @DisplayName("destination 필드 누락 시 예외 처리")
        void withMissingDestination_doesNotForward() {
            // given
            String jsonWithoutDestination = """
                {
                    "payload": {
                        "data": "test"
                    }
                }
                """;
            Message message = createRedisMessage(jsonWithoutDestination);

            // when
            subscriber.onMessage(message, null);

            // then
            then(messagingTemplate).should(never()).convertAndSend(any(String.class), any(Object.class));
        }

        @Test
        @DisplayName("payload 필드 누락 시 예외 처리")
        void withMissingPayload_doesNotForward() {
            // given
            String jsonWithoutPayload = """
                {
                    "destination": "/sub/test"
                }
                """;
            Message message = createRedisMessage(jsonWithoutPayload);

            // when
            subscriber.onMessage(message, null);

            // then
            then(messagingTemplate).should(never()).convertAndSend(any(String.class), any(Object.class));
        }

        @Test
        @DisplayName("빈 메시지 바디 수신 시 예외 처리")
        void withEmptyBody_doesNotForward() {
            // given
            Message message = createRedisMessage("");

            // when
            subscriber.onMessage(message, null);

            // then
            then(messagingTemplate).should(never()).convertAndSend(any(String.class), any(Object.class));
        }
    }

    private Message createRedisMessage(String body) {
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        return message;
    }
}
