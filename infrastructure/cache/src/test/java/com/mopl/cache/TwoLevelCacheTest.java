package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

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
            false
        );
        cache = new TwoLevelCache(CACHE_NAME, l1Cache, redisTemplate, properties, TTL);
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
            var result = cache.get(key);

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
            var result = cache.get(key);

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
            var result = cache.get(key);

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
                false
            );
            cache = new TwoLevelCache(CACHE_NAME, l1Cache, null, properties, TTL);
        }

        @Test
        @DisplayName("RedisTemplate이 null이면 L1만 사용")
        void withNullRedisTemplate_usesL1Only() {
            // given
            String key = "1";
            String fullKey = KEY_PREFIX + CACHE_NAME + "::" + key;
            String value = "user1";

            // when
            cache.put(key, value);

            // then
            then(l1Cache).should().put(fullKey, value);
        }
    }

    @Nested
    @DisplayName("clear()")
    class ClearTest {

        @Test
        @DisplayName("L1과 L2를 모두 클리어")
        void withClear_clearsBothL1AndL2() {
            // given
            String prefix = KEY_PREFIX + CACHE_NAME + "::";
            Set<String> keys = Set.of(prefix + "1", prefix + "2");
            given(l1Cache.asMap()).willReturn(new java.util.concurrent.ConcurrentHashMap<>());
            given(redisTemplate.keys(prefix + "*")).willReturn(keys);
            given(redisTemplate.delete(keys)).willReturn(2L);

            // when
            cache.clear();

            // then
            then(redisTemplate).should().keys(prefix + "*");
            then(redisTemplate).should().delete(keys);
        }
    }
}
