package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TwoLevelCache 단위 테스트")
class TwoLevelCacheTest {

    @Mock
    private Cache<String, Object> l1Cache;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private TwoLevelCache cache;

    private static final String CACHE_NAME = "users";
    private static final String KEY_PREFIX = "mopl:";
    private static final Duration TTL = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        CacheProperties properties = new CacheProperties(
            KEY_PREFIX,
            new L1Config(10000, Duration.ofSeconds(30), true),
            new L2Config(TTL),
            false,
            null
        );
        cache = new TwoLevelCache(CACHE_NAME, l1Cache, redisTemplate, properties, TTL, null);
    }

    @Nested
    @DisplayName("getName()")
    class GetNameTest {

        @Test
        @DisplayName("캐시 이름 반환")
        void returnsName() {
            assertThat(cache.getName()).isEqualTo(CACHE_NAME);
        }
    }

    @Nested
    @DisplayName("getNativeCache()")
    class GetNativeCacheTest {

        @Test
        @DisplayName("L1 캐시 반환")
        void returnsL1Cache() {
            assertThat(cache.getNativeCache()).isSameAs(l1Cache);
        }
    }

    @Nested
    @DisplayName("get()")
    class GetTest {

        @Test
        @DisplayName("L1에 값이 있으면 L1에서 반환")
        void withL1Hit_returnsFromL1() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String expectedValue = "user1";
            given(l1Cache.getIfPresent(fullKey)).willReturn(expectedValue);

            // when
            org.springframework.cache.Cache.ValueWrapper result = cache.get(key);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo(expectedValue);
            then(valueOperations).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("L1 미스, L2 히트시 L2에서 반환하고 L1에 저장")
        void withL1MissAndL2Hit_returnsFromL2AndSavesToL1() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String expectedValue = "user1";
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(expectedValue);

            // when
            org.springframework.cache.Cache.ValueWrapper result = cache.get(key);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo(expectedValue);
            then(l1Cache).should().put(fullKey, expectedValue);
        }

        @Test
        @DisplayName("L1, L2 모두 미스시 null 반환")
        void withL1AndL2Miss_returnsNull() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(null);

            // when
            org.springframework.cache.Cache.ValueWrapper result = cache.get(key);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("get() with valueLoader")
    class GetWithLoaderTest {

        @Test
        @DisplayName("캐시 미스시 valueLoader 호출하고 결과 저장")
        void withCacheMiss_callsLoaderAndSavesResult() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String expectedValue = "user1";
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(null);

            // when
            Object result = cache.get(key, () -> expectedValue);

            // then
            assertThat(result).isEqualTo(expectedValue);
            then(valueOperations).should().set(eq(fullKey), eq(expectedValue), eq(TTL));
            then(l1Cache).should().put(fullKey, expectedValue);
        }

        @Test
        @DisplayName("valueLoader가 예외 발생시 ValueRetrievalException으로 래핑")
        void withLoaderException_wrapsInValueRetrievalException() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> cache.get(key, () -> {
                throw new RuntimeException("DB error");
            })).isInstanceOf(org.springframework.cache.Cache.ValueRetrievalException.class);
        }

        @Test
        @DisplayName("캐시 히트시 valueLoader 호출 안 함")
        void withCacheHit_doesNotCallLoader() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String cachedValue = "cached";
            given(l1Cache.getIfPresent(fullKey)).willReturn(cachedValue);

            // when
            Object result = cache.get(key, () -> "loaded");

            // then
            assertThat(result).isEqualTo(cachedValue);
            then(valueOperations).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("valueLoader가 null 반환시 put 호출 안 함")
        void withLoaderReturningNull_doesNotPut() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(null);

            // when
            Object result = cache.get(key, () -> null);

            // then
            assertThat(result).isNull();
            then(valueOperations).should(never()).set(anyString(), any(), any(Duration.class));
            then(l1Cache).should(never()).put(anyString(), any());
        }
    }

    @Nested
    @DisplayName("put()")
    class PutTest {

        @Test
        @DisplayName("값을 L1과 L2에 모두 저장")
        void withValue_savesToBothL1AndL2() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String value = "user1";
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            cache.put(key, value);

            // then
            then(valueOperations).should().set(fullKey, value, TTL);
            then(l1Cache).should().put(fullKey, value);
        }

        @Test
        @DisplayName("null 값이면 evict 호출")
        void withNullValue_callsEvict() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(redisTemplate.delete(fullKey)).willReturn(true);

            // when
            cache.put(key, null);

            // then
            then(redisTemplate).should().delete(fullKey);
            then(l1Cache).should().invalidate(fullKey);
        }
    }

    @Nested
    @DisplayName("evict()")
    class EvictTest {

        @Test
        @DisplayName("L1과 L2에서 모두 삭제")
        void withKey_deletesFromBothL1AndL2() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(redisTemplate.delete(fullKey)).willReturn(true);

            // when
            cache.evict(key);

            // then
            then(redisTemplate).should().delete(fullKey);
            then(l1Cache).should().invalidate(fullKey);
        }
    }

    @Nested
    @DisplayName("Redis failure")
    class RedisFailureTest {

        @Test
        @DisplayName("Redis 조회 실패시 null 반환하고 서비스 계속")
        void withRedisGetFailure_returnsNullAndContinues() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willThrow(new RuntimeException(
                "Redis connection failed"));

            // when
            Object result = cache.get(key);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Redis 저장 실패시 L1에만 저장하고 서비스 계속")
        void withRedisPutFailure_savesToL1OnlyAndContinues() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String value = "user1";
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            willThrow(new RuntimeException("Redis connection failed"))
                .given(valueOperations).set(anyString(), any(), any(Duration.class));

            // when
            cache.put(key, value);

            // then
            then(l1Cache).should().put(fullKey, value);
        }

        @Test
        @DisplayName("Redis 삭제 실패시 L1만 삭제하고 서비스 계속")
        void withRedisDeleteFailure_deletesFromL1OnlyAndContinues() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            willThrow(new RuntimeException("Redis connection failed"))
                .given(redisTemplate).delete(fullKey);

            // when
            cache.evict(key);

            // then
            then(l1Cache).should().invalidate(fullKey);
        }

        @Test
        @DisplayName("Redis clear 실패시 L1만 클리어하고 서비스 계속")
        void withRedisClearFailure_clearsL1OnlyAndContinues() {
            // given
            String prefix = KEY_PREFIX + CACHE_NAME + "::";
            ConcurrentMap<String, Object> l1Map = new ConcurrentHashMap<>();
            l1Map.put(prefix + "1", "value1");
            given(l1Cache.asMap()).willReturn(l1Map);
            willThrow(new RuntimeException("Redis connection failed"))
                .given(redisTemplate).scan(any(ScanOptions.class));

            // when
            cache.clear();

            // then
            then(l1Cache).should().invalidate(prefix + "1");
        }
    }

    @Nested
    @DisplayName("without Redis")
    class WithoutRedisTest {

        @BeforeEach
        void setUp() {
            CacheProperties properties = new CacheProperties(
                KEY_PREFIX,
                new L1Config(10000, Duration.ofSeconds(30), true),
                new L2Config(TTL),
                false,
                null
            );
            cache = new TwoLevelCache(CACHE_NAME, l1Cache, null, properties, TTL, null);
        }

        @Test
        @DisplayName("RedisTemplate이 null이면 L1만 사용하여 put")
        void withNullRedisTemplate_usesL1OnlyForPut() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String value = "user1";

            // when
            cache.put(key, value);

            // then
            then(l1Cache).should().put(fullKey, value);
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("RedisTemplate이 null이면 L1만 사용하여 lookup")
        void withNullRedisTemplate_usesL1OnlyForLookup() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);

            // when
            org.springframework.cache.Cache.ValueWrapper result = cache.get(key);

            // then
            assertThat(result).isNull();
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("RedisTemplate이 null이면 L1만 사용하여 evict")
        void withNullRedisTemplate_usesL1OnlyForEvict() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;

            // when
            cache.evict(key);

            // then
            then(l1Cache).should().invalidate(fullKey);
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("RedisTemplate이 null이면 L1만 사용하여 clear")
        void withNullRedisTemplate_usesL1OnlyForClear() {
            // given
            String prefix = KEY_PREFIX + CACHE_NAME + "::";
            ConcurrentMap<String, Object> l1Map = new ConcurrentHashMap<>();
            l1Map.put(prefix + "1", "value1");
            l1Map.put(prefix + "2", "value2");
            l1Map.put("other::key", "otherValue");
            given(l1Cache.asMap()).willReturn(l1Map);

            // when
            cache.clear();

            // then
            then(l1Cache).should().invalidate(prefix + "1");
            then(l1Cache).should().invalidate(prefix + "2");
            then(l1Cache).should(never()).invalidate("other::key");
            then(redisTemplate).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("clear()")
    class ClearTest {

        @Mock
        private Cursor<String> cursor;

        @Test
        @DisplayName("L1과 L2를 모두 클리어")
        void withClear_clearsBothL1AndL2() {
            // given
            String prefix = KEY_PREFIX + CACHE_NAME + "::";
            List<String> keys = List.of(prefix + "1", prefix + "2");
            given(l1Cache.asMap()).willReturn(new ConcurrentHashMap<>());
            given(redisTemplate.scan(any(ScanOptions.class))).willReturn(cursor);
            given(cursor.hasNext()).willReturn(true, true, false);
            given(cursor.next()).willReturn(keys.getFirst(), keys.get(1));

            // when
            cache.clear();

            // then
            then(redisTemplate).should().scan(any(ScanOptions.class));
            then(redisTemplate).should().delete(keys.getFirst());
            then(redisTemplate).should().delete(keys.get(1));
        }

        @Test
        @DisplayName("L1에서 prefix가 일치하는 키만 삭제")
        void withClear_deletesOnlyMatchingPrefixFromL1() {
            // given
            String prefix = KEY_PREFIX + CACHE_NAME + "::";
            ConcurrentMap<String, Object> l1Map = new ConcurrentHashMap<>();
            l1Map.put(prefix + "1", "value1");
            l1Map.put(prefix + "2", "value2");
            l1Map.put("other:cache::key", "otherValue");
            given(l1Cache.asMap()).willReturn(l1Map);
            given(redisTemplate.scan(any(ScanOptions.class))).willReturn(cursor);
            given(cursor.hasNext()).willReturn(false);

            // when
            cache.clear();

            // then
            then(l1Cache).should().invalidate(prefix + "1");
            then(l1Cache).should().invalidate(prefix + "2");
            then(l1Cache).should(never()).invalidate("other:cache::key");
        }
    }

    @Nested
    @DisplayName("generateKey()")
    class GenerateKeyTest {

        @Test
        @DisplayName("null 키 입력 시 IllegalArgumentException 발생")
        @SuppressWarnings("DataFlowIssue")
        void withNullKey_throwsException() {
            assertThatThrownBy(() -> cache.get(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cache key must not be null");
        }
    }

    @Nested
    @DisplayName("metrics recording")
    class MetricsRecordingTest {

        @Mock
        private CacheMetrics metrics;

        @Mock
        private Timer.Sample timerSample;

        private TwoLevelCache cacheWithMetrics;

        @BeforeEach
        void setUp() {
            CacheProperties properties = new CacheProperties(
                KEY_PREFIX,
                new L1Config(10000, Duration.ofSeconds(30), true),
                new L2Config(TTL),
                false,
                null
            );
            cacheWithMetrics = new TwoLevelCache(CACHE_NAME, l1Cache, redisTemplate, properties, TTL, metrics);
        }

        @Test
        @DisplayName("L1 히트시 recordL1Hit 호출")
        void withL1Hit_recordsL1Hit() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn("value");

            // when
            cacheWithMetrics.get(key);

            // then
            then(metrics).should().recordL1Hit(CACHE_NAME);
        }

        @Test
        @DisplayName("L2 히트시 recordL2Hit 호출")
        void withL2Hit_recordsL2Hit() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn("value");
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.get(key);

            // then
            then(metrics).should().recordL2Hit(CACHE_NAME);
            then(metrics).should().recordRedisLatency(timerSample, CACHE_NAME, "get");
        }

        @Test
        @DisplayName("캐시 미스시 recordMiss 호출")
        void withCacheMiss_recordsMiss() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willReturn(null);
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.get(key);

            // then
            then(metrics).should().recordMiss(CACHE_NAME);
        }

        @Test
        @DisplayName("put시 recordPut 호출")
        void withPut_recordsPut() {
            // given
            String key = "1";
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.put(key, "value");

            // then
            then(metrics).should().recordPut(CACHE_NAME);
            then(metrics).should().recordRedisLatency(timerSample, CACHE_NAME, "set");
        }

        @Test
        @DisplayName("evict시 recordEvict 호출")
        void withEvict_recordsEvict() {
            // given
            String key = "1";
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.evict(key);

            // then
            then(metrics).should().recordEvict(CACHE_NAME);
            then(metrics).should().recordRedisLatency(timerSample, CACHE_NAME, "delete");
        }

        @Test
        @DisplayName("Redis get 실패시 recordRedisError 호출")
        void withRedisGetFailure_recordsRedisError() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            given(l1Cache.getIfPresent(fullKey)).willReturn(null);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(fullKey)).willThrow(new RuntimeException("Redis error"));
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.get(key);

            // then
            then(metrics).should().recordRedisError(CACHE_NAME, "get");
        }

        @Test
        @DisplayName("Redis set 실패시 recordRedisError 호출")
        void withRedisPutFailure_recordsRedisError() {
            // given
            String key = "1";
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            willThrow(new RuntimeException("Redis error"))
                .given(valueOperations).set(anyString(), any(), any(Duration.class));
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.put(key, "value");

            // then
            then(metrics).should().recordRedisError(CACHE_NAME, "set");
        }

        @Test
        @DisplayName("Redis delete 실패시 recordRedisError 호출")
        void withRedisDeleteFailure_recordsRedisError() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            willThrow(new RuntimeException("Redis error"))
                .given(redisTemplate).delete(fullKey);
            given(metrics.startTimer()).willReturn(timerSample);

            // when
            cacheWithMetrics.evict(key);

            // then
            then(metrics).should().recordRedisError(CACHE_NAME, "delete");
        }

        @Test
        @DisplayName("timerSample이 null이면 recordRedisLatency 호출 안 함")
        void withNullTimerSample_doesNotRecordLatency() {
            // given
            String key = "1";
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(metrics.startTimer()).willReturn(null);

            // when
            cacheWithMetrics.put(key, "value");

            // then
            then(metrics).should(never()).recordRedisLatency(any(), anyString(), anyString());
        }
    }
}
