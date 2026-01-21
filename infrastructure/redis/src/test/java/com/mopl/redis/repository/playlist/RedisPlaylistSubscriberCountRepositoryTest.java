package com.mopl.redis.repository.playlist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisPlaylistSubscriberCountRepository 단위 테스트")
class RedisPlaylistSubscriberCountRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisPlaylistSubscriberCountRepository repository;

    private String buildKey(UUID playlistId) {
        return "playlist:subscriber:count:" + playlistId.toString();
    }

    @Nested
    @DisplayName("getCount()")
    class GetCountTest {

        @Test
        @DisplayName("값이 존재하면 해당 값 반환")
        void withExistingValue_returnsCount() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(100L);

            // when
            long result = repository.getCount(playlistId);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("값이 null이면 0 반환")
        void withNullValue_returnsZero() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(null);

            // when
            long result = repository.getCount(playlistId);

            // then
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("값이 Integer 타입이면 long으로 변환하여 반환")
        void withIntegerValue_returnsLong() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(50);

            // when
            long result = repository.getCount(playlistId);

            // then
            assertThat(result).isEqualTo(50L);
        }

        @Test
        @DisplayName("값이 문자열이면 long으로 파싱하여 반환")
        void withStringValue_returnsLong() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn("200");

            // when
            long result = repository.getCount(playlistId);

            // then
            assertThat(result).isEqualTo(200L);
        }
    }

    @Nested
    @DisplayName("getCounts()")
    class GetCountsTest {

        @Test
        @DisplayName("여러 플레이리스트의 카운트 조회")
        void withMultiplePlaylistIds_returnsCountsMap() {
            // given
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);
            List<String> keys = List.of(buildKey(playlistId1), buildKey(playlistId2));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(keys)).willReturn(List.of(10L, 20L));

            // when
            Map<UUID, Long> result = repository.getCounts(playlistIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(playlistId1)).isEqualTo(10L);
            assertThat(result.get(playlistId2)).isEqualTo(20L);
        }

        @Test
        @DisplayName("빈 목록 조회 시 빈 맵 반환")
        void withEmptyList_returnsEmptyMap() {
            // given
            List<UUID> playlistIds = List.of();

            // when
            Map<UUID, Long> result = repository.getCounts(playlistIds);

            // then
            assertThat(result).isEmpty();
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("multiGet이 null 반환 시 빈 맵 반환")
        void withNullMultiGetResult_returnsEmptyMap() {
            // given
            UUID playlistId = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(any())).willReturn(null);

            // when
            Map<UUID, Long> result = repository.getCounts(playlistIds);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("일부 값이 null이면 해당 카운트는 0으로 처리")
        void withSomeNullValues_returnsZeroForNull() {
            // given
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);
            List<String> keys = List.of(buildKey(playlistId1), buildKey(playlistId2));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(keys)).willReturn(java.util.Arrays.asList(10L, null));

            // when
            Map<UUID, Long> result = repository.getCounts(playlistIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(playlistId1)).isEqualTo(10L);
            assertThat(result.get(playlistId2)).isZero();
        }
    }

    @Nested
    @DisplayName("setCount()")
    class SetCountTest {

        @Test
        @DisplayName("카운트와 TTL 설정")
        void withPlaylistIdAndCount_setsValueWithTtl() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);
            long count = 50L;

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.setCount(playlistId, count);

            // then
            then(valueOperations).should().set(eq(key), eq(count), eq(Duration.ofHours(48)));
        }
    }

    @Nested
    @DisplayName("increment()")
    class IncrementTest {

        @Test
        @DisplayName("카운트 증가")
        void withPlaylistId_incrementsCount() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.increment(playlistId);

            // then
            then(valueOperations).should().increment(key);
        }
    }

    @Nested
    @DisplayName("decrement()")
    class DecrementTest {

        @Test
        @DisplayName("카운트 감소")
        void withPlaylistId_decrementsCount() {
            // given
            UUID playlistId = UUID.randomUUID();
            String key = buildKey(playlistId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.decrement(playlistId);

            // then
            then(valueOperations).should().decrement(key);
        }
    }
}
