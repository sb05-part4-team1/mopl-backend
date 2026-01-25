package com.mopl.sse.interfaces.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.sse.application.SseEmitterManager;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
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
        log.info("Subscribed to Redis channel: {}", DirectMessagePublisher.CHANNEL);
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
                log.debug("Sent SSE direct-message to user: {}", receiverId);
            }
        } catch (Exception e) {
            log.error("Failed to process Redis direct-message", e);
        }
    }
}
