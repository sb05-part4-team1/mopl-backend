package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.domain.service.cache.CacheName;
import com.mopl.domain.service.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
@ConditionalOnProperty(name = "mopl.cache.redis-enabled", havingValue = "false", matchIfMissing = true)
public class LocalCacheServiceImpl implements CacheService {

    private final Cache<String, Object> l1Cache;
    private final CacheProperties properties;

    public LocalCacheServiceImpl(
            Cache<String, Object> l1Cache,
            CacheProperties properties
    ) {
        this.l1Cache = l1Cache;
        this.properties = properties;
        log.info("LocalCacheServiceImpl 초기화 (L1 only, Redis disabled)");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(CacheName cacheName, Object key, Supplier<T> loader) {
        String fullKey = generateKey(cacheName, key);

        Object cached = l1Cache.getIfPresent(fullKey);
        if (cached != null) {
            log.debug("L1 hit: [cache={}, key={}]", cacheName.getValue(), key);
            return (T) cached;
        }

        T loaded = loader.get();
        if (loaded != null) {
            l1Cache.put(fullKey, loaded);
            log.debug("Cache loaded: [cache={}, key={}]", cacheName.getValue(), key);
        }
        return loaded;
    }

    @Override
    public <T> void put(CacheName cacheName, Object key, T value) {
        String fullKey = generateKey(cacheName, key);
        l1Cache.put(fullKey, value);
        log.debug("Cache put: [cache={}, key={}]", cacheName.getValue(), key);
    }

    @Override
    public void evict(CacheName cacheName, Object key) {
        String fullKey = generateKey(cacheName, key);
        l1Cache.invalidate(fullKey);
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

        l1Cache.invalidateAll(fullKeys);
        log.debug("Cache evictAll: [cache={}, keyCount={}]", cacheName.getValue(), keys.size());
    }

    @Override
    public void clear(CacheName cacheName) {
        String prefix = properties.keyPrefix() + cacheName.getValue() + "::";
        l1Cache.asMap().keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .forEach(l1Cache::invalidate);
        log.debug("Cache clear: [cache={}]", cacheName.getValue());
    }

    private String generateKey(CacheName cacheName, Object key) {
        return properties.keyPrefix() + cacheName.getValue() + "::" + key.toString();
    }
}
