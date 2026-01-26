package com.mopl.redis.pubsub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static com.mopl.redis.pubsub.WebSocketMessagePublisher.CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketMessagePublisher 단위 테스트")
class WebSocketMessagePublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private WebSocketMessagePublisher publisher;

    @Nested
    @DisplayName("publish()")
    class PublishTest {

        @Test
        @DisplayName("destination과 payload를 WebSocketMessage로 래핑하여 Redis 채널로 발행")
        void withDestinationAndPayload_publishesWrappedMessage() {
            // given
            String destination = "/sub/contents/123/chat";
            Object payload = new TestPayload("test message");

            // when
            publisher.publish(destination, payload);

            // then
            ArgumentCaptor<WebSocketMessagePublisher.WebSocketMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketMessagePublisher.WebSocketMessage.class);
            then(redisTemplate).should().convertAndSend(
                org.mockito.Mockito.eq(CHANNEL),
                messageCaptor.capture()
            );

            WebSocketMessagePublisher.WebSocketMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.destination()).isEqualTo(destination);
            assertThat(capturedMessage.payload()).isEqualTo(payload);
        }

        @Test
        @DisplayName("채널명이 websocket:messages로 설정됨")
        void channelName_isCorrect() {
            assertThat(CHANNEL).isEqualTo("websocket:messages");
        }
    }

    record TestPayload(String content) {
    }
}
