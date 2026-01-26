package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import com.mopl.domain.support.cache.CacheName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TwoLevelCacheManager 단위 테스트")
class TwoLevelCacheManagerTest {

    @Mock
    private Cache<String, Object> l1Cache;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private TwoLevelCacheManager cacheManager;
    private CacheProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CacheProperties(
            "mopl:",
            new L1Config(10000, Duration.ofSeconds(30), true),
            new L2Config(Duration.ofMinutes(10)),
            false,
            null
        );
        cacheManager = new TwoLevelCacheManager(l1Cache, redisTemplate, properties);
    }

    @Nested
    @DisplayName("getCacheNames()")
    class GetCacheNamesTest {

        @Test
        @DisplayName("CacheName.all()에 정의된 캐시들이 생성됨")
        void withInitialization_createsDefinedCaches() {
            // when
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // then
            assertThat(cacheNames).containsExactlyInAnyOrder(CacheName.all());
        }

        @Test
        @DisplayName("캐시 개수가 CacheName.all()과 일치")
        void withInitialization_matchesCacheCount() {
            // when
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // then
            assertThat(cacheNames).hasSize(CacheName.all().length);
        }

        @Test
        @DisplayName("등록된 모든 캐시 이름 반환")
        void withRegisteredCaches_returnsAllNames() {
            // when
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // then
            assertThat(cacheNames).contains(CacheName.USERS, CacheName.USERS_BY_EMAIL);
        }
    }

    @Nested
    @DisplayName("getCache()")
    class GetCacheTest {

        @Test
        @DisplayName("등록된 캐시 이름으로 조회하면 캐시 반환")
        void withRegisteredName_returnsCache() {
            // when
            org.springframework.cache.Cache cache = cacheManager.getCache(CacheName.USERS);

            // then
            assertThat(cache).isNotNull();
            assertThat(cache.getName()).isEqualTo(CacheName.USERS);
        }

        @Test
        @DisplayName("미등록 캐시 이름으로 조회하면 동적 생성")
        void withUnregisteredName_createsDynamicCache() {
            // given
            String dynamicCacheName = "dynamic-cache";

            // when
            org.springframework.cache.Cache cache = cacheManager.getCache(dynamicCacheName);

            // then
            assertThat(cache).isNotNull();
            assertThat(cache.getName()).isEqualTo(dynamicCacheName);
            assertThat(cacheManager.getCacheNames()).contains(dynamicCacheName);
        }

        @Test
        @DisplayName("같은 이름으로 여러 번 조회해도 동일한 인스턴스 반환")
        void withSameName_returnsSameInstance() {
            // when
            org.springframework.cache.Cache cache1 = cacheManager.getCache(CacheName.USERS);
            org.springframework.cache.Cache cache2 = cacheManager.getCache(CacheName.USERS);

            // then
            assertThat(cache1).isSameAs(cache2);
        }
    }

    @Nested
    @DisplayName("without Redis")
    class WithoutRedisTest {

        @BeforeEach
        void setUp() {
            cacheManager = new TwoLevelCacheManager(l1Cache, null, properties);
        }

        @Test
        @DisplayName("RedisTemplate이 null이어도 정상 초기화")
        void withNullRedisTemplate_initializesSuccessfully() {
            // then
            assertThat(cacheManager.getCacheNames()).isNotEmpty();
        }

        @Test
        @DisplayName("RedisTemplate이 null이어도 캐시 조회 가능")
        void withNullRedisTemplate_retrievesCacheSuccessfully() {
            // when
            org.springframework.cache.Cache cache = cacheManager.getCache(CacheName.USERS);

            // then
            assertThat(cache).isNotNull();
        }
    }
}
