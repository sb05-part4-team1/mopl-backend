package com.mopl.cache;

@FunctionalInterface
public interface CacheInvalidationPublisher {

    void publish(String key);
}
