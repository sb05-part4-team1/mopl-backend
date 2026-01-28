package com.mopl.sse.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.sse.application.SseEmitterManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisDirectMessageSubscriber implements MessageListener {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SseEmitterManager sseEmitterManager;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
            this,
            new ChannelTopic(DirectMessagePublisher.CHANNEL)
        );
        LogContext.with("channel", DirectMessagePublisher.CHANNEL).info("Subscribed to Redis channel");
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            DirectMessageResponse directMessage = objectMapper.readValue(
                message.getBody(), DirectMessageResponse.class
            );

            UUID receiverId = directMessage.receiver().userId();

            if (sseEmitterManager.hasLocalEmitter(receiverId)) {
                sseEmitterManager.sendToUser(
                    receiverId,
                    "direct-messages",
                    directMessage
                );
            }
        } catch (Exception e) {
            LogContext.with("subscriber", "directMessage").error("Failed to process Redis message", e);
        }
    }
}
