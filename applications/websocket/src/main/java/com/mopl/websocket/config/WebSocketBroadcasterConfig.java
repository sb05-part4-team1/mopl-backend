package com.mopl.websocket.config;

import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import com.mopl.websocket.messaging.LocalWebSocketBroadcaster;
import com.mopl.websocket.messaging.RedisWebSocketBroadcaster;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class WebSocketBroadcasterConfig {

    @Bean
    @ConditionalOnProperty(name = "websocket.broadcaster", havingValue = "redis")
    public WebSocketBroadcaster redisWebSocketBroadcaster(WebSocketMessagePublisher publisher) {
        LogContext.with("broadcasterType", "redis").info("WebSocketBroadcaster 초기화 완료");
        return new RedisWebSocketBroadcaster(publisher);
    }

    @Bean
    @ConditionalOnProperty(name = "websocket.broadcaster", havingValue = "local", matchIfMissing = true)
    public WebSocketBroadcaster localWebSocketBroadcaster(SimpMessagingTemplate messagingTemplate) {
        LogContext.with("broadcasterType", "local").info("WebSocketBroadcaster 초기화 완료");
        return new LocalWebSocketBroadcaster(messagingTemplate);
    }
}
