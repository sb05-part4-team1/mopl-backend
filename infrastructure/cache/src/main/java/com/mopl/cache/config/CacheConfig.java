package com.mopl.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mopl.cache.CacheInvalidationPublisher;
import com.mopl.cache.TwoLevelCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;

@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
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
    @ConditionalOnProperty(name = "mopl.cache.redis-enabled", havingValue = "true")
    public ChannelTopic cacheInvalidationTopic(CacheProperties properties) {
        return new ChannelTopic(properties.invalidationChannel());
    }

    @Bean
    @ConditionalOnProperty(name = "mopl.cache.redis-enabled", havingValue = "true")
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        TwoLevelCacheManager cacheManager,
        ChannelTopic cacheInvalidationTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
            (message, pattern) -> {
                String key = new String(message.getBody());
                cacheManager.invalidateL1(key);
            },
            cacheInvalidationTopic
        );
        log.info("Cache invalidation listener registered: [channel={}]",
            cacheInvalidationTopic.getTopic());
        return container;
    }

    @Bean
    @ConditionalOnProperty(name = "mopl.cache.redis-enabled", havingValue = "true")
    public CacheInvalidationPublisher cacheInvalidationPublisher(
        RedisTemplate<String, Object> redisTemplate,
        ChannelTopic cacheInvalidationTopic
    ) {
        return key -> {
            try {
                redisTemplate.convertAndSend(cacheInvalidationTopic.getTopic(), key);
            } catch (Exception e) {
                log.warn("Failed to publish cache invalidation: [key={}]", key, e);
            }
        };
    }

    @Bean
    public CacheManager cacheManager(
        Cache<String, Object> caffeineCache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        @Nullable CacheInvalidationPublisher cacheInvalidationPublisher
    ) {
        return new TwoLevelCacheManager(
            caffeineCache,
            properties.redisEnabled() ? redisTemplate : null,
            properties,
            cacheInvalidationPublisher
        );
    }
}
