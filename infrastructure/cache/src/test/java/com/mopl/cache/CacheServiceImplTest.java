package com.mopl.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.mopl.cache.config.CacheProperties;
import com.mopl.cache.config.CacheProperties.L1Config;
import com.mopl.cache.config.CacheProperties.L2Config;
import com.mopl.domain.service.cache.CacheName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheServiceImpl 테스트")
class CacheServiceImplTest {

    @Mock
    private Cache<String, Object> l1Cache;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private RedisMessageListenerContainer listenerContainer;

    private CacheServiceImpl cacheService;

    private static final String KEY_PREFIX = "mopl:";
    private static final String INVALIDATION_CHANNEL = "cache:invalidation";

    @BeforeEach
    void setUp() {
        L1Config l1Config = new L1Config(10000, Duration.ofMinutes(1), true);
        L2Config l2Config = new L2Config(Duration.ofMinutes(10));
        CacheProperties properties = new CacheProperties(KEY_PREFIX, l1Config, l2Config,
            INVALIDATION_CHANNEL, true);
        ChannelTopic invalidationTopic = new ChannelTopic(INVALIDATION_CHANNEL);

        cacheService = new CacheServiceImpl(
            l1Cache,
            redisTemplate,
            properties,
            invalidationTopic,
            listenerContainer
        );
    }

    private String generateKey(CacheName cacheName, Object key) {
        return KEY_PREFIX + cacheName.getValue() + "::" + key.toString();
    }

    @Nested
    @DisplayName("get 메서드 테스트")
    class GetTest {

        @Test
        @DisplayName("L1 캐시에 값이 있으면 L1에서 반환한다")
        void get_L1Hit() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String cachedValue = "user-data";

            when(l1Cache.getIfPresent(fullKey)).thenReturn(cachedValue);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> "loaded-value");

            // then
            assertThat(result).isEqualTo(cachedValue);
            verify(l1Cache).getIfPresent(fullKey);
            verify(redisTemplate, never()).opsForValue();
        }

        @Test
        @DisplayName("L1 미스, L2 히트 시 L2에서 반환하고 L1에 저장한다")
        void get_L2Hit() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String cachedValue = "user-data-from-redis";

            when(l1Cache.getIfPresent(fullKey)).thenReturn(null);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(fullKey)).thenReturn(cachedValue);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> "loaded-value");

            // then
            assertThat(result).isEqualTo(cachedValue);
            verify(l1Cache).getIfPresent(fullKey);
            verify(valueOperations).get(fullKey);
            verify(l1Cache).put(fullKey, cachedValue);
        }

        @Test
        @DisplayName("L1, L2 모두 미스 시 로더를 호출하고 양쪽에 저장한다")
        void get_CacheMiss_LoadAndStore() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String loadedValue = "loaded-value";

            when(l1Cache.getIfPresent(fullKey)).thenReturn(null);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(fullKey)).thenReturn(null);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> loadedValue);

            // then
            assertThat(result).isEqualTo(loadedValue);
            verify(valueOperations).set(eq(fullKey), eq(loadedValue), eq(CacheName.USERS.getTtl()));
            verify(l1Cache).put(fullKey, loadedValue);
        }

        @Test
        @DisplayName("로더가 null을 반환하면 캐시에 저장하지 않는다")
        void get_LoaderReturnsNull() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);

            when(l1Cache.getIfPresent(fullKey)).thenReturn(null);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(fullKey)).thenReturn(null);

            // when
            String result = cacheService.get(CacheName.USERS, userId, () -> null);

            // then
            assertThat(result).isNull();
            verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
            verify(l1Cache, never()).put(anyString(), any());
        }
    }

    @Nested
    @DisplayName("put 메서드 테스트")
    class PutTest {

        @Test
        @DisplayName("값을 L1, L2에 저장하고 무효화 메시지를 발행한다")
        void put_Success() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);
            String value = "user-data";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when
            cacheService.put(CacheName.USERS, userId, value);

            // then
            verify(valueOperations).set(eq(fullKey), eq(value), eq(CacheName.USERS.getTtl()));
            verify(l1Cache).put(fullKey, value);
            verify(redisTemplate).convertAndSend(INVALIDATION_CHANNEL, fullKey);
        }
    }

    @Nested
    @DisplayName("evict 메서드 테스트")
    class EvictTest {

        @Test
        @DisplayName("L1, L2에서 삭제하고 무효화 메시지를 발행한다")
        void evict_Success() {
            // given
            Long userId = 1L;
            String fullKey = generateKey(CacheName.USERS, userId);

            // when
            cacheService.evict(CacheName.USERS, userId);

            // then
            verify(redisTemplate).delete(fullKey);
            verify(l1Cache).invalidate(fullKey);
            verify(redisTemplate).convertAndSend(INVALIDATION_CHANNEL, fullKey);
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("evictAll 메서드 테스트")
    class EvictAllTest {

        @Test
        @DisplayName("여러 키를 한 번에 삭제하고 각각 무효화 메시지를 발행한다")
        void evictAll_Success() {
            // given
            List<Long> userIds = List.of(1L, 2L, 3L);
            List<String> fullKeys = userIds.stream()
                .map(id -> generateKey(CacheName.USERS, id))
                .toList();

            // when
            cacheService.evictAll(CacheName.USERS, userIds);

            // then
            verify(redisTemplate).delete(fullKeys);
            verify(l1Cache).invalidateAll(fullKeys);
            verify(redisTemplate, times(3)).convertAndSend(eq(INVALIDATION_CHANNEL), anyString());
        }

        @Test
        @DisplayName("빈 컬렉션이 주어지면 아무것도 하지 않는다")
        void evictAll_EmptyCollection() {
            // when
            cacheService.evictAll(CacheName.USERS, List.of());

            // then
            verify(redisTemplate, never()).delete(any(List.class));
            verify(l1Cache, never()).invalidateAll(any());
        }

        @Test
        @DisplayName("null이 주어지면 아무것도 하지 않는다")
        void evictAll_NullCollection() {
            // when
            cacheService.evictAll(CacheName.USERS, null);

            // then
            verify(redisTemplate, never()).delete(any(List.class));
            verify(l1Cache, never()).invalidateAll(any());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("clear 메서드 테스트")
    class ClearTest {

        @Test
        @DisplayName("패턴에 매칭되는 모든 키를 삭제하고 무효화 메시지를 발행한다")
        void clear_Success() {
            // given
            String pattern = KEY_PREFIX + CacheName.USERS.getValue() + "::*";
            Set<String> matchedKeys = Set.of(
                generateKey(CacheName.USERS, 1L),
                generateKey(CacheName.USERS, 2L)
            );

            when(redisTemplate.keys(pattern)).thenReturn(matchedKeys);

            // when
            cacheService.clear(CacheName.USERS);

            // then
            verify(redisTemplate).keys(pattern);
            verify(redisTemplate).delete(matchedKeys);
            verify(l1Cache).invalidateAll(matchedKeys);
            verify(redisTemplate, times(2)).convertAndSend(eq(INVALIDATION_CHANNEL), anyString());
        }

        @Test
        @DisplayName("매칭되는 키가 없으면 삭제하지 않는다")
        void clear_NoMatchingKeys() {
            // given
            String pattern = KEY_PREFIX + CacheName.USERS.getValue() + "::*";

            when(redisTemplate.keys(pattern)).thenReturn(Set.of());

            // when
            cacheService.clear(CacheName.USERS);

            // then
            verify(redisTemplate).keys(pattern);
            verify(redisTemplate, never()).delete(any(Set.class));
            verify(l1Cache, never()).invalidateAll(any());
        }
    }
}
