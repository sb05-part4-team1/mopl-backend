package com.mopl.sse.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.NotificationPublisher;
import com.mopl.sse.application.SseEmitterManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SseEmitterManager sseEmitterManager;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
            this,
            new ChannelTopic(NotificationPublisher.CHANNEL)
        );
        LogContext.with("channel", NotificationPublisher.CHANNEL).info("Subscribed to Redis channel");
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            NotificationModel notification = objectMapper.readValue(
                message.getBody(), NotificationModel.class
            );

            if (sseEmitterManager.hasLocalEmitter(notification.getReceiverId())) {
                sseEmitterManager.sendToUser(
                    notification.getReceiverId(),
                    "notifications",
                    notification
                );
            }
        } catch (Exception e) {
            LogContext.with("subscriber", "notification").error("Failed to process Redis message", e);
        }
    }
}
