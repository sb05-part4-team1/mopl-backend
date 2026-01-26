package com.mopl.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
public class WebSocketMessagePublisher {

    public static final String CHANNEL = "websocket:messages";

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String destination, Object payload) {
        WebSocketMessage message = new WebSocketMessage(destination, payload);
        redisTemplate.convertAndSend(CHANNEL, message);
    }

    public record WebSocketMessage(String destination, Object payload) {
    }
}
