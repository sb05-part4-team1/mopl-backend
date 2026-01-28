package com.mopl.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.TwoLevelCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("CacheConfig 단위 테스트")
class CacheConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(CacheConfig.class)
        .withPropertyValues(
            "mopl.cache.key-prefix=mopl:",
            "mopl.cache.l1.maximum-size=10000",
            "mopl.cache.l1.ttl=30s",
            "mopl.cache.l1.record-stats=true",
            "mopl.cache.l2.default-ttl=10m",
            "mopl.cache.redis-enabled=false"
        );

    @Nested
    @DisplayName("caffeineCache()")
    class CaffeineCacheTest {

        @Test
        @DisplayName("CacheProperties로 Caffeine 캐시 생성")
        void withProperties_createsCaffeineCache() {
            contextRunner.run(context -> {
                assertThat(context).hasSingleBean(Cache.class);
                @SuppressWarnings("unchecked")
                Cache<String, Object> cache = context.getBean(Cache.class);
                assertThat(cache).isNotNull();
            });
        }

        @Test
        @DisplayName("recordStats가 true면 통계 기록 활성화")
        void withRecordStatsTrue_enablesStats() {
            contextRunner.run(context -> {
                @SuppressWarnings("unchecked")
                Cache<String, Object> cache = context.getBean(Cache.class);
                cache.put("key", "value");
                cache.getIfPresent("key");
                assertThat(cache.stats().hitCount()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("recordStats가 false면 통계 기록 비활성화")
        void withRecordStatsFalse_disablesStats() {
            new ApplicationContextRunner()
                .withUserConfiguration(CacheConfig.class)
                .withPropertyValues(
                    "mopl.cache.key-prefix=mopl:",
                    "mopl.cache.l1.maximum-size=10000",
                    "mopl.cache.l1.ttl=30s",
                    "mopl.cache.l1.record-stats=false",
                    "mopl.cache.l2.default-ttl=10m",
                    "mopl.cache.redis-enabled=false"
                )
                .run(context -> {
                    @SuppressWarnings("unchecked")
                    Cache<String, Object> cache = context.getBean(Cache.class);
                    cache.put("key", "value");
                    cache.getIfPresent("key");
                    assertThat(cache.stats().hitCount()).isZero();
                });
        }
    }

    @Nested
    @DisplayName("cacheManager()")
    class CacheManagerTest {

        @Test
        @DisplayName("TwoLevelCacheManager 생성")
        void withDependencies_createsTwoLevelCacheManager() {
            contextRunner.run(context -> {
                assertThat(context).hasSingleBean(CacheManager.class);
                assertThat(context.getBean(CacheManager.class))
                    .isInstanceOf(TwoLevelCacheManager.class);
            });
        }

        @Test
        @DisplayName("RedisTemplate이 있으면 주입")
        void withRedisTemplate_injectsRedisTemplate() {
            new ApplicationContextRunner()
                .withUserConfiguration(CacheConfig.class, RedisTemplateConfig.class)
                .withPropertyValues(
                    "mopl.cache.key-prefix=mopl:",
                    "mopl.cache.l1.maximum-size=10000",
                    "mopl.cache.l1.ttl=30s",
                    "mopl.cache.l1.record-stats=true",
                    "mopl.cache.l2.default-ttl=10m",
                    "mopl.cache.redis-enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class))
                        .isInstanceOf(TwoLevelCacheManager.class);
                });
        }

        @Test
        @DisplayName("RedisTemplate이 없어도 정상 생성")
        void withoutRedisTemplate_createsSuccessfully() {
            contextRunner.run(context -> assertThat(context).hasSingleBean(CacheManager.class));
        }
    }

    @Configuration
    static class RedisTemplateConfig {
        @Bean
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate() {
            return mock(RedisTemplate.class);
        }
    }
}
