package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.logging.context.LogContext;
import io.micrometer.core.instrument.Timer;
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
    private final CacheMetrics metrics;

    public TwoLevelCache(
        String name,
        Cache<String, Object> l1Cache,
        @Nullable RedisTemplate<String, Object> redisTemplate,
        CacheProperties properties,
        Duration ttl,
        @Nullable CacheMetrics metrics
    ) {
        super(true);
        this.name = name;
        this.l1Cache = l1Cache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.ttl = ttl;
        this.metrics = metrics;
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
            recordL1Hit();
            return l1Value;
        }

        Object l2Value = getFromRedis(fullKey);
        if (l2Value != null) {
            LogContext.with("cache", name).and("key", key).debug("L2 hit");
            recordL2Hit();
            l1Cache.put(fullKey, l2Value);
            return l2Value;
        }

        recordMiss();
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
        recordPut();

        LogContext.with("cache", name).and("key", key).and("ttl", ttl).and("redis", redisSuccess).debug("Cache put");
    }

    @Override
    public void evict(@NonNull Object key) {
        String fullKey = generateKey(key);
        deleteFromRedis(fullKey);
        l1Cache.invalidate(fullKey);
        recordEvict();

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
        Timer.Sample sample = startTimer();
        try {
            Object value = redisTemplate.opsForValue().get(key);
            recordRedisLatency(sample, "get");
            return value;
        } catch (Exception e) {
            recordRedisLatency(sample, "get");
            recordRedisError("get");
            LogContext.with("key", key).warn("Redis get failed: " + e.getMessage());
            return null;
        }
    }

    private boolean putToRedis(String key, Object value) {
        if (redisTemplate == null) {
            return false;
        }
        Timer.Sample sample = startTimer();
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            recordRedisLatency(sample, "set");
            return true;
        } catch (Exception e) {
            recordRedisLatency(sample, "set");
            recordRedisError("set");
            LogContext.with("key", key).error("Redis put failed", e);
            return false;
        }
    }

    private void deleteFromRedis(String key) {
        if (redisTemplate == null) {
            return;
        }
        Timer.Sample sample = startTimer();
        try {
            redisTemplate.delete(key);
            recordRedisLatency(sample, "delete");
        } catch (Exception e) {
            recordRedisLatency(sample, "delete");
            recordRedisError("delete");
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

    private void recordL1Hit() {
        if (metrics != null) {
            metrics.recordL1Hit(name);
        }
    }

    private void recordL2Hit() {
        if (metrics != null) {
            metrics.recordL2Hit(name);
        }
    }

    private void recordMiss() {
        if (metrics != null) {
            metrics.recordMiss(name);
        }
    }

    private void recordPut() {
        if (metrics != null) {
            metrics.recordPut(name);
        }
    }

    private void recordEvict() {
        if (metrics != null) {
            metrics.recordEvict(name);
        }
    }

    private void recordRedisError(String operation) {
        if (metrics != null) {
            metrics.recordRedisError(name, operation);
        }
    }

    private Timer.Sample startTimer() {
        return metrics != null ? metrics.startTimer() : null;
    }

    private void recordRedisLatency(Timer.Sample sample, String operation) {
        if (metrics != null && sample != null) {
            metrics.recordRedisLatency(sample, name, operation);
        }
    }
}
