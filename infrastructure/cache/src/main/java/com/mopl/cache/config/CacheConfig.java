package com.mopl.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mopl.cache.TwoLevelCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
public class CacheConfig {

    @Bean
    public Cache<String, Object> caffeineCache(CacheProperties properties) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .maximumSize(properties.l1().maximumSize())
            .expireAfterWrite(properties.l1().ttl());

        if (properties.l1().recordStats()) {
            builder.recordStats();
        }

        return builder.build();
    }

    @Bean
    public CacheManager cacheManager(
        Cache<String, Object> caffeineCache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties
    ) {
        return new TwoLevelCacheManager(
            caffeineCache,
            properties.redisEnabled() ? redisTemplate : null,
            properties
        );
    }
}
