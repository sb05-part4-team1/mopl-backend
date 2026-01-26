package com.mopl.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "websocket")
public record WebSocketProperties(
    String allowedOrigins,
    BroadcasterType broadcaster
) {

    public enum BroadcasterType {
        local, redis
    }
}
