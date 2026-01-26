package com.mopl.redis.pubsub;

import com.mopl.domain.model.notification.NotificationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    public static final String CHANNEL = "sse:notifications";

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(NotificationModel notification) {
        redisTemplate.convertAndSend(CHANNEL, notification);
    }

    public void publishAll(List<NotificationModel> notifications) {
        notifications.forEach(this::publish);
    }
}
