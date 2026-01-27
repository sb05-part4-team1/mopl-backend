package com.mopl.domain.support.cache;

public interface CachePort {

    void put(String cacheName, Object key, Object value);
}
