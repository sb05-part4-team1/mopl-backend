package com.mopl.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CacheMetrics 단위 테스트")
class CacheMetricsTest {

    private MeterRegistry registry;
    private CacheMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new CacheMetrics(registry);
    }

    @Nested
    @DisplayName("recordL1Hit()")
    class RecordL1HitTest {

        @Test
        @DisplayName("L1 히트 카운터 증가")
        void withCacheName_incrementsL1HitCounter() {
            // when
            metrics.recordL1Hit("users");
            metrics.recordL1Hit("users");

            // then
            Counter counter = registry.find("mopl.cache.hit")
                .tag("cache", "users")
                .tag("level", "l1")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(2.0);
        }
    }

    @Nested
    @DisplayName("recordL2Hit()")
    class RecordL2HitTest {

        @Test
        @DisplayName("L2 히트 카운터 증가")
        void withCacheName_incrementsL2HitCounter() {
            // when
            metrics.recordL2Hit("users");

            // then
            Counter counter = registry.find("mopl.cache.hit")
                .tag("cache", "users")
                .tag("level", "l2")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("recordMiss()")
    class RecordMissTest {

        @Test
        @DisplayName("미스 카운터 증가")
        void withCacheName_incrementsMissCounter() {
            // when
            metrics.recordMiss("users");

            // then
            Counter counter = registry.find("mopl.cache.miss")
                .tag("cache", "users")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("recordPut()")
    class RecordPutTest {

        @Test
        @DisplayName("put 카운터 증가")
        void withCacheName_incrementsPutCounter() {
            // when
            metrics.recordPut("users");

            // then
            Counter counter = registry.find("mopl.cache.put")
                .tag("cache", "users")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("recordEvict()")
    class RecordEvictTest {

        @Test
        @DisplayName("evict 카운터 증가")
        void withCacheName_incrementsEvictCounter() {
            // when
            metrics.recordEvict("users");

            // then
            Counter counter = registry.find("mopl.cache.evict")
                .tag("cache", "users")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("recordRedisError()")
    class RecordRedisErrorTest {

        @Test
        @DisplayName("Redis 에러 카운터 증가")
        void withCacheNameAndOperation_incrementsErrorCounter() {
            // when
            metrics.recordRedisError("users", "get");

            // then
            Counter counter = registry.find("mopl.cache.redis.error")
                .tag("cache", "users")
                .tag("operation", "get")
                .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("startTimer() / recordRedisLatency()")
    class TimerTest {

        @Test
        @DisplayName("타이머 시작 및 레이턴시 기록")
        void withTimerSample_recordsLatency() {
            // given
            Timer.Sample sample = metrics.startTimer();

            // when
            metrics.recordRedisLatency(sample, "users", "get");

            // then
            Timer timer = registry.find("mopl.cache.redis.latency")
                .tag("cache", "users")
                .tag("operation", "get")
                .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }
    }
}
