package com.mopl.sse.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisEmitterRepository 단위 테스트")
class RedisEmitterRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private RedisEmitterRepository redisEmitterRepository;

    @BeforeEach
    void setUp() {
        redisEmitterRepository = new RedisEmitterRepository(redisTemplate);
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("emitter를 저장하면 localEmitters에 추가된다")
        void savesEmitterToLocalMap() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = new SseEmitter();

            // when
            redisEmitterRepository.save(userId, emitter);

            // then
            assertThat(redisEmitterRepository.getLocalEmitters()).containsKey(userId);
            assertThat(redisEmitterRepository.getLocalEmitters().get(userId)).isEqualTo(emitter);
        }
    }

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserIdTest {

        @Test
        @DisplayName("존재하는 userId로 조회하면 emitter 반환")
        void withExistingUserId_returnsEmitter() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = new SseEmitter();
            redisEmitterRepository.save(userId, emitter);

            // when
            Optional<SseEmitter> result = redisEmitterRepository.findByUserId(userId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(emitter);
        }

        @Test
        @DisplayName("존재하지 않는 userId로 조회하면 빈 Optional 반환")
        void withNonExistingUserId_returnsEmpty() {
            // given
            UUID userId = UUID.randomUUID();

            // when
            Optional<SseEmitter> result = redisEmitterRepository.findByUserId(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUserId()")
    class DeleteByUserIdTest {

        @Test
        @DisplayName("userId로 emitter 삭제")
        void deletesEmitter() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = new SseEmitter();
            redisEmitterRepository.save(userId, emitter);

            // when
            redisEmitterRepository.deleteByUserId(userId);

            // then
            assertThat(redisEmitterRepository.getLocalEmitters()).doesNotContainKey(userId);
        }
    }

    @Nested
    @DisplayName("existsLocally()")
    class ExistsLocallyTest {

        @Test
        @DisplayName("로컬에 emitter가 존재하면 true 반환")
        void whenExists_returnsTrue() {
            // given
            UUID userId = UUID.randomUUID();
            SseEmitter emitter = new SseEmitter();
            redisEmitterRepository.save(userId, emitter);

            // when
            boolean result = redisEmitterRepository.existsLocally(userId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("로컬에 emitter가 존재하지 않으면 false 반환")
        void whenNotExists_returnsFalse() {
            // given
            UUID userId = UUID.randomUUID();

            // when
            boolean result = redisEmitterRepository.existsLocally(userId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("cacheEvent()")
    class CacheEventTest {

        @Test
        @DisplayName("이벤트를 Redis에 캐싱")
        void cachesEventToRedis() {
            // given
            UUID userId = UUID.randomUUID();
            UUID eventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            Object eventData = "test data";

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.size(anyString())).willReturn(1L);

            // when
            redisEmitterRepository.cacheEvent(userId, eventId, eventData);

            // then
            then(zSetOperations).should().add(eq("sse:events:" + userId), any(), anyDouble());
            then(redisTemplate).should().expire(eq("sse:events:" + userId), any());
        }

        @Test
        @DisplayName("캐시 크기가 최대치 초과 시 오래된 이벤트 삭제")
        void whenCacheSizeExceedsMax_removesOldEvents() {
            // given
            UUID userId = UUID.randomUUID();
            UUID eventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            Object eventData = "test data";

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.size(anyString())).willReturn(150L);

            // when
            redisEmitterRepository.cacheEvent(userId, eventId, eventData);

            // then
            then(zSetOperations).should().removeRange(eq("sse:events:" + userId), eq(0L), anyLong());
        }
    }

    @Nested
    @DisplayName("getEventsAfter()")
    class GetEventsAfterTest {

        @Test
        @DisplayName("lastEventId 이후의 이벤트 조회")
        void returnsEventsAfterLastEventId() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");
            UUID cachedEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdf0");
            RedisEmitterRepository.CachedEvent cachedEvent = new RedisEmitterRepository.CachedEvent(cachedEventId, "cached data");

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
                .willReturn(Set.of(ZSetOperations.TypedTuple.of(cachedEvent, 1.0)));

            // when
            List<RedisEmitterRepository.CachedEvent> result = redisEmitterRepository.getEventsAfter(userId, lastEventId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().eventId()).isEqualTo(cachedEventId);
        }

        @Test
        @DisplayName("캐시된 이벤트가 없으면 빈 리스트 반환")
        void whenNoEvents_returnsEmptyList() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.fromString("01934567-89ab-7def-0123-456789abcdef");

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
                .willReturn(null);

            // when
            List<RedisEmitterRepository.CachedEvent> result = redisEmitterRepository.getEventsAfter(userId, lastEventId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLocalEmitters()")
    class GetLocalEmittersTest {

        @Test
        @DisplayName("로컬 emitter 맵 반환")
        void returnsLocalEmittersMap() {
            // given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            SseEmitter emitter1 = new SseEmitter();
            SseEmitter emitter2 = new SseEmitter();

            redisEmitterRepository.save(userId1, emitter1);
            redisEmitterRepository.save(userId2, emitter2);

            // when
            Map<UUID, SseEmitter> result = redisEmitterRepository.getLocalEmitters();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsKeys(userId1, userId2);
        }
    }
}
