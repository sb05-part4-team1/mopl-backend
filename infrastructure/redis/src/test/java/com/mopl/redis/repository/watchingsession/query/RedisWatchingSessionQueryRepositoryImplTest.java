package com.mopl.redis.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.redis.support.WatchingSessionRedisKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisWatchingSessionQueryRepositoryImpl 단위 테스트")
class RedisWatchingSessionQueryRepositoryImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private RedisWatchingSessionQueryRepositoryImpl repository;

    private WatchingSessionQueryRequest createRequest(
        String cursor,
        UUID idAfter,
        int limit,
        SortDirection direction
    ) {
        return new WatchingSessionQueryRequest(
            null,
            cursor,
            idAfter,
            limit,
            direction,
            WatchingSessionSortField.CREATED_AT
        );
    }

    private WatchingSessionModel createModel(UUID watcherId, UUID contentId, Instant createdAt) {
        return WatchingSessionModel.builder()
            .watcherId(watcherId)
            .contentId(contentId)
            .watcherName("Watcher_" + watcherId.toString().substring(0, 8))
            .createdAt(createdAt)
            .build();
    }

    @Nested
    @DisplayName("findAllByContentId()")
    class FindAllByContentIdTest {

        @Test
        @DisplayName("세션이 없으면 빈 응답 반환")
        void withNoSessions_returnsEmptyResponse() {
            // given
            UUID contentId = UUID.randomUUID();
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(0L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(Set.of());

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("ASC 정렬로 세션 목록 조회")
        void withAscendingOrder_returnsSortedSessions() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId1 = UUID.randomUUID();
            UUID watcherId2 = UUID.randomUUID();
            Instant time1 = Instant.parse("2024-01-01T10:00:00Z");
            Instant time2 = Instant.parse("2024-01-01T11:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId1.toString(), (double) time1.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(watcherId2.toString(), (double) time2.toEpochMilli()));

            WatchingSessionModel model1 = createModel(watcherId1, contentId, time1);
            WatchingSessionModel model2 = createModel(watcherId2, contentId, time2);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model1, model2));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data().get(0).getCreatedAt()).isEqualTo(time1);
            assertThat(result.data().get(1).getCreatedAt()).isEqualTo(time2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(2L);
            assertThat(result.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("DESC 정렬로 세션 목록 조회")
        void withDescendingOrder_returnsSortedSessions() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId1 = UUID.randomUUID();
            UUID watcherId2 = UUID.randomUUID();
            Instant time1 = Instant.parse("2024-01-01T11:00:00Z");
            Instant time2 = Instant.parse("2024-01-01T10:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.DESCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId1.toString(), (double) time1.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(watcherId2.toString(), (double) time2.toEpochMilli()));

            WatchingSessionModel model1 = createModel(watcherId1, contentId, time1);
            WatchingSessionModel model2 = createModel(watcherId2, contentId, time2);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.reverseRangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model1, model2));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data().get(0).getCreatedAt()).isEqualTo(time1);
            assertThat(result.data().get(1).getCreatedAt()).isEqualTo(time2);
            assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("limit보다 많은 결과가 있으면 hasNext=true")
        void withMoreResultsThanLimit_hasNextIsTrue() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId1 = UUID.randomUUID();
            UUID watcherId2 = UUID.randomUUID();
            UUID watcherId3 = UUID.randomUUID();
            Instant time1 = Instant.parse("2024-01-01T10:00:00Z");
            Instant time2 = Instant.parse("2024-01-01T11:00:00Z");
            Instant time3 = Instant.parse("2024-01-01T12:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 2, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId1.toString(), (double) time1.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(watcherId2.toString(), (double) time2.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(watcherId3.toString(), (double) time3.toEpochMilli()));

            WatchingSessionModel model1 = createModel(watcherId1, contentId, time1);
            WatchingSessionModel model2 = createModel(watcherId2, contentId, time2);
            WatchingSessionModel model3 = createModel(watcherId3, contentId, time3);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(10L);
            given(zSetOperations.rangeWithScores(eq(zsetKey), eq(0L), eq(3L))).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model1, model2, model3));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(time2.toString());
            assertThat(result.nextIdAfter()).isEqualTo(watcherId2);
            assertThat(result.totalCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("커서 기반 페이지네이션으로 다음 페이지 조회")
        void withCursor_returnsNextPage() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId = UUID.randomUUID();
            Instant cursorTime = Instant.parse("2024-01-01T10:00:00Z");
            Instant time = Instant.parse("2024-01-01T11:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(
                cursorTime.toString(),
                UUID.randomUUID(),
                10,
                SortDirection.ASCENDING
            );

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(watcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(5L);
            given(zSetOperations.rangeByScoreWithScores(
                eq(zsetKey),
                eq((double) cursorTime.toEpochMilli()),
                eq(Double.POSITIVE_INFINITY),
                eq(0L),
                eq(20L)
            )).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(watcherId);
        }

        @Test
        @DisplayName("ZSet에서 null이 반환되면 빈 응답")
        void withNullTuples_returnsEmptyResponse() {
            // given
            UUID contentId = UUID.randomUUID();
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(0L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(null);

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("잘못된 UUID 형식은 필터링됨")
        void withInvalidUuid_filtersOut() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID validWatcherId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>("invalid-uuid", (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(validWatcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(validWatcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(validWatcherId);
        }

        @Test
        @DisplayName("multiGet에서 null이 반환되면 빈 결과")
        void withNullMultiGetResult_returnsEmptyModels() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId.toString(), (double) time.toEpochMilli()));

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(1L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(null);

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("DESC 정렬에서 커서 기반 페이지네이션")
        void withCursorDescending_returnsNextPage() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId = UUID.randomUUID();
            Instant cursorTime = Instant.parse("2024-01-01T12:00:00Z");
            Instant time = Instant.parse("2024-01-01T10:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(
                cursorTime.toString(),
                UUID.randomUUID(),
                10,
                SortDirection.DESCENDING
            );

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(watcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(5L);
            given(zSetOperations.reverseRangeByScoreWithScores(
                eq(zsetKey),
                eq(Double.NEGATIVE_INFINITY),
                eq((double) cursorTime.toEpochMilli()),
                eq(0L),
                eq(20L)
            )).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(watcherId);
            assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("빈 문자열 UUID는 필터링됨")
        void withEmptyStringUuid_filtersOut() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID validWatcherId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>("", (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>("   ", (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(validWatcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(validWatcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(3L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(validWatcherId);
        }

        @Test
        @DisplayName("null 값을 가진 tuple은 필터링됨")
        void withNullValueTuple_filtersOut() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID validWatcherId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(null, (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(validWatcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(validWatcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(validWatcherId);
        }

        @Test
        @DisplayName("multiGet 결과에 WatchingSessionModel이 아닌 타입이 섞여 있으면 필터링됨")
        void withMixedTypesInMultiGet_filtersNonModels() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId1 = UUID.randomUUID();
            UUID watcherId2 = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId1.toString(), (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>(watcherId2.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(watcherId1, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model, "invalid-type"));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(watcherId1);
        }

        @Test
        @DisplayName("zCard가 null이면 totalCount는 0")
        void withNullZCard_totalCountIsZero() {
            // given
            UUID contentId = UUID.randomUUID();
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(null);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(Set.of());

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("watcherIds가 비어있으면 빈 결과 반환")
        void withEmptyWatcherIds_returnsEmptyResult() {
            // given
            UUID contentId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            // 모든 tuple의 값이 null이거나 유효하지 않은 UUID인 경우
            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(null, (double) time.toEpochMilli()));
            tuples.add(new DefaultTypedTuple<>("invalid-uuid", (double) time.toEpochMilli()));

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("cursor만 있고 idAfter가 null이면 처음부터 조회")
        void withCursorButNoIdAfter_fetchesFromStart() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID watcherId = UUID.randomUUID();
            Instant cursorTime = Instant.parse("2024-01-01T10:00:00Z");
            Instant time = Instant.parse("2024-01-01T11:00:00Z");

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            // cursor는 있지만 idAfter는 null
            WatchingSessionQueryRequest request = createRequest(
                cursorTime.toString(),
                null,
                10,
                SortDirection.ASCENDING
            );

            Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
            tuples.add(new DefaultTypedTuple<>(watcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(watcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(1L);
            // idAfter가 null이므로 fetchFromStart가 호출되어야 함
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(watcherId);
        }

        @Test
        @DisplayName("tuple 자체가 null인 경우 필터링됨")
        void withNullTupleInSet_filtersOut() {
            // given
            UUID contentId = UUID.randomUUID();
            UUID validWatcherId = UUID.randomUUID();
            Instant time = Instant.now();

            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
            WatchingSessionQueryRequest request = createRequest(null, null, 10, SortDirection.ASCENDING);

            // HashSet은 null을 허용하므로 null tuple 추가 가능
            Set<ZSetOperations.TypedTuple<Object>> tuples = new java.util.HashSet<>();
            tuples.add(null);
            tuples.add(new DefaultTypedTuple<>(validWatcherId.toString(), (double) time.toEpochMilli()));

            WatchingSessionModel model = createModel(validWatcherId, contentId, time);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(2L);
            given(zSetOperations.rangeWithScores(zsetKey, 0, 19)).willReturn(tuples);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.multiGet(anyList())).willReturn(List.of(model));

            // when
            CursorResponse<WatchingSessionModel> result = repository.findAllByContentId(contentId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().getWatcherId()).isEqualTo(validWatcherId);
        }
    }
}
