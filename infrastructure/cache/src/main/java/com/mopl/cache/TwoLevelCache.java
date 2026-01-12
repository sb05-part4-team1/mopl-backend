package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
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
            log.debug("L1 hit: [cache={}, key={}]", name, key);
            return l1Value;
        }

        Object l2Value = getFromRedis(fullKey);
        if (l2Value != null) {
            log.debug("L2 hit: [cache={}, key={}]", name, key);
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
            log.debug("Cache loaded: [cache={}, key={}]", name, key);
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

        log.debug("Cache put: [cache={}, key={}, ttl={}, redis={}]", name, key, ttl, redisSuccess);
    }

    @Override
    public void evict(@NonNull Object key) {
        String fullKey = generateKey(key);
        deleteFromRedis(fullKey);
        l1Cache.invalidate(fullKey);

        log.debug("Cache evict: [cache={}, key={}]", name, key);
    }

    @Override
    public void clear() {
        String prefix = properties.keyPrefix() + name + "::";

        l1Cache.asMap().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .forEach(l1Cache::invalidate);

        clearRedis(prefix);

        log.debug("Cache clear: [cache={}]", name);
    }

    private String generateKey(Object key) {
        return properties.keyPrefix() + name + "::" + key.toString();
    }

    @Nullable
    private Object getFromRedis(String key) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get failed: [key={}] {}", key, e.getMessage());
            return null;
        }
    }

    private boolean putToRedis(String key, Object value) {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Redis put success: [key={}]", key);
            return true;
        } catch (Exception e) {
            log.error("Redis put failed: [key={}]", key, e);
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
            log.warn("Redis delete failed: [key={}] {}", key, e.getMessage());
        }
    }

    private void clearRedis(String prefix) {
        if (redisTemplate == null) {
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis clear failed: [prefix={}] {}", prefix, e.getMessage());
        }
    }
}
