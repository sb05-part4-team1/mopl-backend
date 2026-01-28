package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.logging.context.LogContext;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TwoLevelCacheManager implements CacheManager {

    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final CacheMetrics metrics;
    private final Map<String, TwoLevelCache> caches = new ConcurrentHashMap<>();

    public TwoLevelCacheManager(
        Cache<String, Object> l1Cache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        @Nullable CacheMetrics metrics
    ) {
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.metrics = metrics;

        for (String cacheName : CacheName.all()) {
            caches.put(cacheName, createCache(cacheName));
        }

        LogContext.with("caches", caches.keySet())
            .and("redisEnabled", redisTemplate != null)
            .info("TwoLevelCacheManager initialized");
    }

    @Override
    @Nullable
    public org.springframework.cache.Cache getCache(@NonNull String name) {
        return caches.computeIfAbsent(name, this::createCache);
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    private TwoLevelCache createCache(String name) {
        return new TwoLevelCache(
            name,
            l1Cache,
            redisTemplate,
            properties,
            properties.getTtlFor(name),
            metrics
        );
    }
}
