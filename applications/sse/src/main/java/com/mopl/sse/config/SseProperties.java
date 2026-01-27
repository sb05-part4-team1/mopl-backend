package com.mopl.sse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("mopl.sse")
public record SseProperties(
    EventCacheConfig eventCache
) {

    public SseProperties {
        if (eventCache == null) {
            eventCache = new EventCacheConfig(Duration.ofMinutes(5), 100);
        }
    }

    public record EventCacheConfig(
        Duration ttl,
        int maxSize
    ) {

        public EventCacheConfig {
            if (ttl == null) {
                ttl = Duration.ofMinutes(5);
            }
            if (maxSize <= 0) {
                maxSize = 100;
            }
        }
    }
}
