package com.mopl.domain.support.cache;

public interface CachePort {

    void evict(String cacheName, Object key);
}
