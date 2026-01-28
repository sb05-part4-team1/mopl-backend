package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.logging.context.LogContext;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;

public class TwoLevelCache extends AbstractValueAdaptingCache {

    private final String name;
    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;
    private final Duration ttl;

    public TwoLevelCache(
        String name,
        Cache<String, Object> l1Cache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        Duration ttl
    ) {
        super(true);
        this.name = name;
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.ttl = ttl;
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

        Object l1Value = l1Cache.getIfPresent(fullKey);
        if (l1Value != null) {
            LogContext.with("cache", name).and("key", key).debug("L1 hit");
            return l1Value;
        }

        Object l2Value = getFromRedis(fullKey);
        if (l2Value != null) {
            LogContext.with("cache", name).and("key", key).debug("L2 hit");
            l1Cache.put(fullKey, l2Value);
            return l2Value;
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
            T loadedValue = valueLoader.call();
            if (loadedValue != null) {
                put(key, loadedValue);
            }
            LogContext.with("cache", name).and("key", key).debug("Cache loaded");
            return loadedValue;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        if (value == null) {
            evict(key);
            return;
        }

        String fullKey = generateKey(key);
        boolean redisSuccess = putToRedis(fullKey, value);
        l1Cache.put(fullKey, value);

        LogContext.with("cache", name).and("key", key).and("ttl", ttl).and("redis", redisSuccess).debug("Cache put");
    }

    @Override
    public void evict(@NonNull Object key) {
        String fullKey = generateKey(key);
        deleteFromRedis(fullKey);
        l1Cache.invalidate(fullKey);

        LogContext.with("cache", name).and("key", key).debug("Cache evict");
    }

    @Override
    public void clear() {
        String prefix = properties.keyPrefix() + name + "::";

        l1Cache.asMap().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .forEach(l1Cache::invalidate);

        clearRedis(prefix);

        LogContext.with("cache", name).debug("Cache clear");
    }

    private String generateKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key must not be null");
        }
        return properties.keyPrefix() + name + "::" + key;
    }

    @Nullable
    private Object getFromRedis(String key) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LogContext.with("key", key).warn("Redis get failed: " + e.getMessage());
            return null;
        }
    }

    private boolean putToRedis(String key, Object value) {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            return true;
        } catch (Exception e) {
            LogContext.with("key", key).error("Redis put failed", e);
            return false;
        }
    }

    private void deleteFromRedis(String key) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            LogContext.with("key", key).warn("Redis delete failed: " + e.getMessage());
        }
    }

    private void clearRedis(String prefix) {
        if (redisTemplate == null) {
            return;
        }
        try {
            ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(100)
                .build();

            try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    redisTemplate.delete(cursor.next());
                }
            }
        } catch (Exception e) {
            LogContext.with("prefix", prefix).warn("Redis clear failed: " + e.getMessage());
        }
    }
}
