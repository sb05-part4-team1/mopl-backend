package com.mopl.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CacheMetrics {

    private final MeterRegistry registry;

    public CacheMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordL1Hit(String cacheName) {
        Counter.builder("mopl.cache.hit")
            .tag("cache", cacheName)
            .tag("level", "l1")
            .register(registry)
            .increment();
    }

    public void recordL2Hit(String cacheName) {
        Counter.builder("mopl.cache.hit")
            .tag("cache", cacheName)
            .tag("level", "l2")
            .register(registry)
            .increment();
    }

    public void recordMiss(String cacheName) {
        Counter.builder("mopl.cache.miss")
            .tag("cache", cacheName)
            .register(registry)
            .increment();
    }

    public void recordPut(String cacheName) {
        Counter.builder("mopl.cache.put")
            .tag("cache", cacheName)
            .register(registry)
            .increment();
    }

    public void recordEvict(String cacheName) {
        Counter.builder("mopl.cache.evict")
            .tag("cache", cacheName)
            .register(registry)
            .increment();
    }

    public void recordRedisError(String cacheName, String operation) {
        Counter.builder("mopl.cache.redis.error")
            .tag("cache", cacheName)
            .tag("operation", operation)
            .register(registry)
            .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordRedisLatency(Timer.Sample sample, String cacheName, String operation) {
        sample.stop(Timer.builder("mopl.cache.redis.latency")
            .tag("cache", cacheName)
            .tag("operation", operation)
            .register(registry));
    }
}
