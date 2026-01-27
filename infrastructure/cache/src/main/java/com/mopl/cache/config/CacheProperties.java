package com.mopl.cache.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties("mopl.cache")
@Validated
public record CacheProperties(
    @NotBlank String keyPrefix,
    @NotNull @Valid L1Config l1,
    @NotNull @Valid L2Config l2,
    boolean redisEnabled,
    Map<String, Duration> ttl
) {

    public record L1Config(
        @Positive long maximumSize,
        @NotNull Duration ttl,
        boolean recordStats
    ) {
    }

    public record L2Config(
        @NotNull Duration defaultTtl
    ) {
    }

    public Duration getTtlFor(String cacheName) {
        if (ttl == null) {
            return l2.defaultTtl();
        }
        return ttl.getOrDefault(cacheName, l2.defaultTtl());
    }
}
