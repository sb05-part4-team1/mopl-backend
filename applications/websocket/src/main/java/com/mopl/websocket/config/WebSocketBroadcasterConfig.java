package com.mopl.websocket.config;

import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import com.mopl.websocket.messaging.LocalWebSocketBroadcaster;
import com.mopl.websocket.messaging.RedisWebSocketBroadcaster;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
@Slf4j
public class WebSocketBroadcasterConfig {

    @Bean
    @Primary
    @ConditionalOnBean(WebSocketMessagePublisher.class)
    public WebSocketBroadcaster redisWebSocketBroadcaster(WebSocketMessagePublisher publisher) {
        log.info("Using RedisWebSocketBroadcaster for distributed WebSocket messaging");
        return new RedisWebSocketBroadcaster(publisher);
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketBroadcaster.class)
    public WebSocketBroadcaster localWebSocketBroadcaster(SimpMessagingTemplate messagingTemplate) {
        log.info("Using LocalWebSocketBroadcaster (single-server mode)");
        return new LocalWebSocketBroadcaster(messagingTemplate);
    }
}
