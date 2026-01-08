package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.domain.support.cache.CacheName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TwoLevelCacheManager implements CacheManager {

    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final CacheInvalidationPublisher invalidationPublisher;
    private final Map<String, TwoLevelCache> caches = new ConcurrentHashMap<>();

    public TwoLevelCacheManager(
        Cache<String, Object> l1Cache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        @Nullable CacheInvalidationPublisher invalidationPublisher
    ) {
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.invalidationPublisher = invalidationPublisher;

        Arrays.stream(CacheName.values()).forEach(cacheName ->
            caches.put(cacheName.getValue(), createCache(cacheName))
        );

        log.info("TwoLevelCacheManager initialized: [caches={}]", caches.keySet());
    }

    @Override
    @Nullable
    public org.springframework.cache.Cache getCache(@NonNull String name) {
        return caches.computeIfAbsent(name, this::createDynamicCache);
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    public void invalidateL1(String fullKey) {
        caches.values().forEach(cache -> cache.invalidateL1(fullKey));
    }

    private TwoLevelCache createCache(CacheName cacheName) {
        return new TwoLevelCache(
            cacheName.getValue(),
            l1Cache,
            redisTemplate,
            properties,
            cacheName.getTtl(),
            invalidationPublisher
        );
    }

    private TwoLevelCache createDynamicCache(String name) {
        log.warn("Creating dynamic cache not defined in CacheName: [name={}]", name);
        return new TwoLevelCache(
            name,
            l1Cache,
            redisTemplate,
            properties,
            properties.l2().defaultTtl(),
            invalidationPublisher
        );
    }
}
