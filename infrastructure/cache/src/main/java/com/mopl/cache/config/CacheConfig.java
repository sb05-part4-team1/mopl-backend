package com.mopl.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
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
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
