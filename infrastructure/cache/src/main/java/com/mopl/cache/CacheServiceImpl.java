package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.domain.service.cache.CacheName;
import com.mopl.domain.service.cache.CacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Slf4j
@ConditionalOnProperty(name = "mopl.cache.redis-enabled", havingValue = "true")
public class CacheServiceImpl implements CacheService {

    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final ChannelTopic invalidationTopic;
    private final RedisMessageListenerContainer listenerContainer;

    public CacheServiceImpl(
        Cache<String, Object> l1Cache,
        RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        ChannelTopic invalidationTopic,
        RedisMessageListenerContainer listenerContainer
    ) {
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.invalidationTopic = invalidationTopic;
        this.listenerContainer = listenerContainer;
    }

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(
            (message, pattern) -> {
                String key = new String(message.getBody());
                l1Cache.invalidate(key);
                log.debug("L1 invalidated by Pub/Sub: [key={}]", key);
            },
            invalidationTopic
        );
        log.info("Cache invalidation listener registered: [channel={}]", invalidationTopic.getTopic());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(CacheName cacheName, Object key, Supplier<T> loader) {
        String fullKey = generateKey(cacheName, key);

        // 1. L1 (Caffeine) 조회
        Object l1Value = l1Cache.getIfPresent(fullKey);
        if (l1Value != null) {
            log.debug("L1 hit: [cache={}, key={}]", cacheName.getValue(), key);
            return (T) l1Value;
        }

        // 2. L2 (Redis) 조회
        Object l2Value = redisTemplate.opsForValue().get(fullKey);
        if (l2Value != null) {
            log.debug("L2 hit: [cache={}, key={}]", cacheName.getValue(), key);
            l1Cache.put(fullKey, l2Value);  // L1에 채우기
            return (T) l2Value;
        }

        // 3. Loader 호출
        T loaded = loader.get();
        if (loaded != null) {
            Duration ttl = getTtl(cacheName);
            redisTemplate.opsForValue().set(fullKey, loaded, ttl);  // L2에 저장
            l1Cache.put(fullKey, loaded);  // L1에 저장
            log.debug("Cache loaded: [cache={}, key={}, ttl={}]", cacheName.getValue(), key, ttl);
        }
        return loaded;
    }

    @Override
    public <T> void put(CacheName cacheName, Object key, T value) {
        String fullKey = generateKey(cacheName, key);
        Duration ttl = getTtl(cacheName);

        redisTemplate.opsForValue().set(fullKey, value, ttl);  // L2
        l1Cache.put(fullKey, value);  // L1
        publishInvalidation(fullKey);  // 다른 서버 L1 무효화

        log.debug("Cache put: [cache={}, key={}, ttl={}]", cacheName.getValue(), key, ttl);
    }

    @Override
    public void evict(CacheName cacheName, Object key) {
        String fullKey = generateKey(cacheName, key);

        redisTemplate.delete(fullKey);  // L2
        l1Cache.invalidate(fullKey);  // L1
        publishInvalidation(fullKey);  // 다른 서버 L1 무효화

        log.debug("Cache evict: [cache={}, key={}]", cacheName.getValue(), key);
    }

    @Override
    public void evictAll(CacheName cacheName, Collection<?> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<String> fullKeys = keys.stream()
            .map(key -> generateKey(cacheName, key))
            .toList();

        redisTemplate.delete(fullKeys);
        l1Cache.invalidateAll(fullKeys);
        fullKeys.forEach(this::publishInvalidation);

        log.debug("Cache evictAll: [cache={}, keyCount={}]", cacheName.getValue(), keys.size());
    }

    @Override
    public void clear(CacheName cacheName) {
        String pattern = properties.keyPrefix() + cacheName.getValue() + "::*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            l1Cache.invalidateAll(keys);
            keys.forEach(this::publishInvalidation);
            log.debug("Cache clear: [cache={}, keyCount={}]", cacheName.getValue(), keys.size());
        }
    }

    private String generateKey(CacheName cacheName, Object key) {
        return properties.keyPrefix() + cacheName.getValue() + "::" + key.toString();
    }

    private Duration getTtl(CacheName cacheName) {
        Duration ttl = cacheName.getTtl();
        return ttl != null ? ttl : properties.l2().defaultTtl();
    }

    private void publishInvalidation(String key) {
        redisTemplate.convertAndSend(invalidationTopic.getTopic(), key);
    }
}
