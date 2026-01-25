package com.mopl.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessagePublisher {

    public static final String CHANNEL = "sse:direct-messages";

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(Object directMessage) {
        redisTemplate.convertAndSend(CHANNEL, directMessage);
    }
}
