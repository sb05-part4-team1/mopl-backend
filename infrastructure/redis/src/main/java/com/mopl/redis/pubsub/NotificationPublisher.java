package com.mopl.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(NotificationMessage message) {
        redisTemplate.convertAndSend(NotificationMessage.CHANNEL, message);
    }
}
