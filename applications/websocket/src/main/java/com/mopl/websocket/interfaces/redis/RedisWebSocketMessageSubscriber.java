package com.mopl.websocket.interfaces.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "websocket.broadcaster", havingValue = "redis")
@RequiredArgsConstructor
public class RedisWebSocketMessageSubscriber implements MessageListener {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        redisMessageListenerContainer.addMessageListener(
            this,
            new ChannelTopic(WebSocketMessagePublisher.CHANNEL)
        );
        LogContext.with("channel", WebSocketMessagePublisher.CHANNEL).info("Subscribed to Redis channel");
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            JsonNode rootNode = objectMapper.readTree(message.getBody());
            String destination = rootNode.get("destination").asText();
            JsonNode payload = rootNode.get("payload");

            messagingTemplate.convertAndSend(destination, payload);
            LogContext.with("destination", destination).debug("Forwarded Redis message to WebSocket");
        } catch (Exception e) {
            LogContext.with("subscriber", "websocket").error("Failed to process Redis message", e);
        }
    }
}
