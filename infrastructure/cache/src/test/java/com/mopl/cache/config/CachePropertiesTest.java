package com.mopl.cache.config;

import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CacheProperties 단위 테스트")
class CachePropertiesTest {

    @Nested
    @DisplayName("getTtlFor()")
    class GetTtlForTest {

        @Test
        @DisplayName("ttl 맵에 캐시 이름이 있으면 해당 TTL 반환")
        void withExistingCacheName_returnsConfiguredTtl() {
            // given
            Duration usersTtl = Duration.ofMinutes(5);
            Duration defaultTtl = Duration.ofMinutes(10);
            CacheProperties properties = new CacheProperties(
                "mopl:",
                new L1Config(10000, Duration.ofSeconds(30), true),
                new L2Config(defaultTtl),
                true,
                Map.of("users", usersTtl)
            );

            // when
            Duration result = properties.getTtlFor("users");

            // then
            assertThat(result).isEqualTo(usersTtl);
        }

        @Test
        @DisplayName("ttl 맵에 캐시 이름이 없으면 기본 TTL 반환")
        void withNonExistingCacheName_returnsDefaultTtl() {
            // given
            Duration defaultTtl = Duration.ofMinutes(10);
            CacheProperties properties = new CacheProperties(
                "mopl:",
                new L1Config(10000, Duration.ofSeconds(30), true),
                new L2Config(defaultTtl),
                true,
                Map.of("users", Duration.ofMinutes(5))
            );

            // when
            Duration result = properties.getTtlFor("playlists");

            // then
            assertThat(result).isEqualTo(defaultTtl);
        }

        @Test
        @DisplayName("ttl 맵이 null이면 기본 TTL 반환")
        void withNullTtlMap_returnsDefaultTtl() {
            // given
            Duration defaultTtl = Duration.ofMinutes(10);
            CacheProperties properties = new CacheProperties(
                "mopl:",
                new L1Config(10000, Duration.ofSeconds(30), true),
                new L2Config(defaultTtl),
                true,
                null
            );

            // when
            Duration result = properties.getTtlFor("users");

            // then
            assertThat(result).isEqualTo(defaultTtl);
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessorsTest {

        @Test
        @DisplayName("모든 필드에 접근 가능")
        void withAllFields_accessesCorrectly() {
            // given
            String keyPrefix = "mopl:";
            L1Config l1 = new L1Config(10000, Duration.ofSeconds(30), true);
            L2Config l2 = new L2Config(Duration.ofMinutes(10));
            boolean redisEnabled = true;
            Map<String, Duration> ttl = Map.of("users", Duration.ofMinutes(5));

            CacheProperties properties = new CacheProperties(keyPrefix, l1, l2, redisEnabled, ttl);

            // then
            assertThat(properties.keyPrefix()).isEqualTo(keyPrefix);
            assertThat(properties.l1()).isEqualTo(l1);
            assertThat(properties.l2()).isEqualTo(l2);
            assertThat(properties.redisEnabled()).isTrue();
            assertThat(properties.ttl()).isEqualTo(ttl);
        }
    }

    @Nested
    @DisplayName("L1Config")
    class L1ConfigTest {

        @Test
        @DisplayName("L1Config 필드 접근")
        void withAllFields_accessesCorrectly() {
            // given
            L1Config config = new L1Config(10000, Duration.ofSeconds(30), true);

            // then
            assertThat(config.maximumSize()).isEqualTo(10000);
            assertThat(config.ttl()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.recordStats()).isTrue();
        }
    }

    @Nested
    @DisplayName("L2Config")
    class L2ConfigTest {

        @Test
        @DisplayName("L2Config 필드 접근")
        void withDefaultTtl_accessesCorrectly() {
            // given
            L2Config config = new L2Config(Duration.ofMinutes(10));

            // then
            assertThat(config.defaultTtl()).isEqualTo(Duration.ofMinutes(10));
        }
    }
}
