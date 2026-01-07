package com.mopl.domain.service.cache;

import java.util.Collection;
import java.util.function.Supplier;

public interface CacheService {

    <T> T get(CacheName cacheName, Object key, Supplier<T> loader);

    <T> void put(CacheName cacheName, Object key, T value);

    void evict(CacheName cacheName, Object key);

    void evictAll(CacheName cacheName, Collection<?> keys);

    void clear(CacheName cacheName);
}
