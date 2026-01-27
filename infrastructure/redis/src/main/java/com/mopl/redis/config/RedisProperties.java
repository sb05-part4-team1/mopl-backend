package com.mopl.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("mopl.redis")
public record RedisProperties(
    WatchingSessionConfig watchingSession
) {

    public record WatchingSessionConfig(
        Duration ttl
    ) {
    }
}
