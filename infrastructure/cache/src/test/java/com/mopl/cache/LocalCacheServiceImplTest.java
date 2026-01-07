package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mopl.cache.config.CacheProperties;
import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import com.mopl.domain.service.cache.CacheName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LocalCacheServiceImpl 테스트")
class LocalCacheServiceImplTest {

    private Cache<String, Object> l1Cache;
    private LocalCacheServiceImpl cacheService;
    private CacheProperties properties;

    private static final String KEY_PREFIX = "mopl:";

    @BeforeEach
    void setUp() {
        l1Cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();

        L1Config l1Config = new L1Config(1000, Duration.ofMinutes(1), false);
        L2Config l2Config = new L2Config(Duration.ofMinutes(10));
        properties = new CacheProperties(KEY_PREFIX, l1Config, l2Config, "cache:invalidation", false);

        cacheService = new LocalCacheServiceImpl(l1Cache, properties);
    }

    private String generateKey(CacheName cacheName, Object key) {
        return KEY_PREFIX + cacheName.getValue() + "::" + key.toString();
    }

    @Nested
    @DisplayName("get 메서드 테스트")
    class GetTest {

        @Test
        @DisplayName("캐시에 값이 있으면 캐시에서 반환한다")
        void get_CacheHit() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String cachedValue = "user-data";
            l1Cache.put(fullKey, cachedValue);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> "loaded-value");

            // then
            assertThat(result).isEqualTo(cachedValue);
        }

        @Test
        @DisplayName("캐시 미스 시 로더를 호출하고 캐시에 저장한다")
        void get_CacheMiss() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String loadedValue = "loaded-value";

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> loadedValue);

            // then
            assertThat(result).isEqualTo(loadedValue);
            assertThat(l1Cache.getIfPresent(fullKey)).isEqualTo(loadedValue);
        }

        @Test
        @DisplayName("로더가 null을 반환하면 캐시에 저장하지 않는다")
        void get_LoaderReturnsNull() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> null);

            // then
            assertThat(result).isNull();
            assertThat(l1Cache.getIfPresent(fullKey)).isNull();
        }
    }

    @Nested
    @DisplayName("put 메서드 테스트")
    class PutTest {

        @Test
        @DisplayName("값을 캐시에 저장한다")
        void put_Success() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String value = "user-data";

            // when
            cacheService.put(CacheName.USERS, userId, value);

            // then
            assertThat(l1Cache.getIfPresent(fullKey)).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("evict 메서드 테스트")
    class EvictTest {

        @Test
        @DisplayName("캐시에서 값을 삭제한다")
        void evict_Success() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            l1Cache.put(fullKey, "user-data");

            // when
            cacheService.evict(CacheName.USERS, userId);

            // then
            assertThat(l1Cache.getIfPresent(fullKey)).isNull();
        }
    }

    @Nested
    @DisplayName("evictAll 메서드 테스트")
    class EvictAllTest {

        @Test
        @DisplayName("여러 키를 한 번에 삭제한다")
        void evictAll_Success() {
            // given
            List<Long> userIds = List.of(1L, 2L, 3L);
            userIds.forEach(id -> l1Cache.put(generateKey(CacheName.USERS, id), "user-" + id));

            // when
            cacheService.evictAll(CacheName.USERS, userIds);

            // then
            userIds.forEach(id ->
                assertThat(l1Cache.getIfPresent(generateKey(CacheName.USERS, id))).isNull()
            );
        }

        @Test
        @DisplayName("빈 컬렉션이 주어지면 아무것도 하지 않는다")
        void evictAll_EmptyCollection() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            l1Cache.put(fullKey, "user-data");

            // when
            cacheService.evictAll(CacheName.USERS, List.of());

            // then
            assertThat(l1Cache.getIfPresent(fullKey)).isEqualTo("user-data");
        }
    }

    @Nested
    @DisplayName("clear 메서드 테스트")
    class ClearTest {

        @Test
        @DisplayName("해당 캐시의 모든 키를 삭제한다")
        void clear_Success() {
            // given
            l1Cache.put(generateKey(CacheName.USERS, 1L), "user-1");
            l1Cache.put(generateKey(CacheName.USERS, 2L), "user-2");
            l1Cache.put(KEY_PREFIX + "other::1", "other-data");

            // when
            cacheService.clear(CacheName.USERS);

            // then
            assertThat(l1Cache.getIfPresent(generateKey(CacheName.USERS, 1L))).isNull();
            assertThat(l1Cache.getIfPresent(generateKey(CacheName.USERS, 2L))).isNull();
            assertThat(l1Cache.getIfPresent(KEY_PREFIX + "other::1")).isEqualTo("other-data");
        }
    }
}
