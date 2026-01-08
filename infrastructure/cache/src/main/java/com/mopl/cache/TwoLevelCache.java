package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;

@Slf4j
public class TwoLevelCache extends AbstractValueAdaptingCache {

    private final String name;
    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final Duration ttl;
    private final CacheInvalidationPublisher invalidationPublisher;

    public TwoLevelCache(
        String name,
        Cache<String, Object> l1Cache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        Duration ttl,
        @Nullable CacheInvalidationPublisher invalidationPublisher
    ) {
        super(true);
        this.name = name;
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.ttl = ttl;
        this.invalidationPublisher = invalidationPublisher;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return l1Cache;
    }

    @Override
    @Nullable
    protected Object lookup(@NonNull Object key) {
        String fullKey = generateKey(key);

        // L1 조회
        Object l1Value = l1Cache.getIfPresent(fullKey);
        if (l1Value != null) {
            log.debug("L1 hit: [cache={}, key={}]", name, key);
            return l1Value;
        }

        // L2 조회 (Redis가 있는 경우만)
        if (redisTemplate != null) {
            Object l2Value = redisTemplate.opsForValue().get(fullKey);
            if (l2Value != null) {
                log.debug("L2 hit: [cache={}, key={}]", name, key);
                l1Cache.put(fullKey, l2Value);
                return l2Value;
            }
        }

        return null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        Object cached = lookup(key);
        if (cached != null) {
            return (T) cached;
        }

        try {
            T value = valueLoader.call();
            if (value != null) {
                put(key, value);
            }
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        if (value == null) {
            return;
        }

        String fullKey = generateKey(key);

        // L2 저장 (Redis가 있는 경우)
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(fullKey, value, ttl);
        }

        // L1 저장
        l1Cache.put(fullKey, value);

        // 다른 서버 L1 무효화
        publishInvalidation(fullKey);

        log.debug("Cache put: [cache={}, key={}, ttl={}]", name, key, ttl);
    }

    @Override
    public void evict(@NonNull Object key) {
        String fullKey = generateKey(key);

        // L2 삭제
        if (redisTemplate != null) {
            redisTemplate.delete(fullKey);
        }

        // L1 삭제
        l1Cache.invalidate(fullKey);

        // 다른 서버 L1 무효화
        publishInvalidation(fullKey);

        log.debug("Cache evict: [cache={}, key={}]", name, key);
    }

    @Override
    public void clear() {
        String prefix = properties.keyPrefix() + name + "::";

        // L1에서 해당 캐시의 키만 삭제
        l1Cache.asMap().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .forEach(k -> {
                l1Cache.invalidate(k);
                publishInvalidation(k);
            });

        log.debug("Cache clear: [cache={}]", name);
    }

    private String generateKey(Object key) {
        return properties.keyPrefix() + name + "::" + key.toString();
    }

    private void publishInvalidation(String key) {
        if (invalidationPublisher != null) {
            invalidationPublisher.publish(key);
        }
    }

    public void invalidateL1(String fullKey) {
        l1Cache.invalidate(fullKey);
        log.debug("L1 invalidated by Pub/Sub: [key={}]", fullKey);
    }
}
