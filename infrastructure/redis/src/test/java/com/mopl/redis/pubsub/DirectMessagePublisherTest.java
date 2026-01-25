package com.mopl.redis.pubsub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static com.mopl.redis.pubsub.DirectMessagePublisher.CHANNEL;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessagePublisher 단위 테스트")
class DirectMessagePublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private DirectMessagePublisher publisher;

    @Nested
    @DisplayName("publish()")
    class PublishTest {

        @Test
        @DisplayName("DirectMessage를 Redis 채널로 발행")
        void withDirectMessage_publishesToChannel() {
            // given
            Object directMessage = new Object();

            // when
            publisher.publish(directMessage);

            // then
            then(redisTemplate).should().convertAndSend(CHANNEL, directMessage);
        }
    }
}
