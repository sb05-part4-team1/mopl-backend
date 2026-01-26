package com.mopl.redis.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.redis.support.WatchingSessionRedisKeys;
import com.mopl.redis.support.cursor.RedisCursorPaginationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisWatchingSessionQueryRepositoryImpl implements WatchingSessionQueryRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public CursorResponse<WatchingSessionModel> findAllByContentId(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);
        WatchingSessionSortFieldSupport sortField = WatchingSessionSortFieldSupport.from(
            request.sortBy()
        );

        Long totalCount = redisTemplate.opsForZSet().zCard(zsetKey);
        long total = totalCount != null ? totalCount : 0L;

        int limit = request.limit();
        int fetchSize = Math.min(limit * 2, 500);

        List<WatchingSessionModel> candidates = fetchCandidates(zsetKey, request, sortField, fetchSize);

        List<WatchingSessionModel> afterCursor = RedisCursorPaginationHelper.applyCursor(
            candidates,
            request,
            sortField,
            sortField::extractValue,
            WatchingSessionModel::getWatcherId
        );

        return RedisCursorPaginationHelper.buildResponse(
            afterCursor,
            request,
            sortField,
            total,
            sortField::extractValue,
            WatchingSessionModel::getWatcherId
        );
    }

    private List<WatchingSessionModel> fetchCandidates(
        String zsetKey,
        WatchingSessionQueryRequest request,
        WatchingSessionSortFieldSupport sortField,
        int fetchSize
    ) {
        SortDirection direction = request.sortDirection();
        Instant cursorInstant = sortField.deserializeCursor(request.cursor());

        Set<ZSetOperations.TypedTuple<Object>> tuples;
        if (cursorInstant == null || request.idAfter() == null) {
            tuples = fetchFromStart(zsetKey, direction, fetchSize);
        } else {
            tuples = fetchFromCursorScore(zsetKey, direction, cursorInstant, fetchSize);
        }

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<UUID> watcherIds = tuples.stream()
            .filter(tuple -> tuple != null && tuple.getValue() != null)
            .map(tuple -> parseUuid(tuple.getValue().toString()))
            .filter(Objects::nonNull)
            .toList();

        if (watcherIds.isEmpty()) {
            return List.of();
        }

        List<String> sessionKeys = watcherIds.stream()
            .map(WatchingSessionRedisKeys::watcherSessionKey)
            .toList();

        List<Object> storedSessions = redisTemplate.opsForValue().multiGet(sessionKeys);

        List<WatchingSessionModel> models = new ArrayList<>(watcherIds.size());
        if (storedSessions != null) {
            for (Object stored : storedSessions) {
                if (stored instanceof WatchingSessionModel model) {
                    models.add(model);
                }
            }
        }

        return models.stream()
            .sorted((a, b) -> RedisCursorPaginationHelper.compareByFieldThenId(
                a, b,
                sortField::extractValue,
                WatchingSessionModel::getWatcherId,
                direction
            ))
            .toList();
    }

    private Set<ZSetOperations.TypedTuple<Object>> fetchFromStart(
        String zsetKey,
        SortDirection direction,
        int fetchSize
    ) {
        if (direction.isAscending()) {
            return redisTemplate.opsForZSet().rangeWithScores(zsetKey, 0, fetchSize - 1);
        }
        return redisTemplate.opsForZSet().reverseRangeWithScores(zsetKey, 0, fetchSize - 1);
    }

    private Set<ZSetOperations.TypedTuple<Object>> fetchFromCursorScore(
        String zsetKey,
        SortDirection direction,
        Instant cursorInstant,
        int fetchSize
    ) {
        double cursorScore = cursorInstant.toEpochMilli();

        if (direction.isAscending()) {
            return redisTemplate.opsForZSet().rangeByScoreWithScores(
                zsetKey,
                cursorScore,
                Double.POSITIVE_INFINITY,
                0,
                fetchSize
            );
        }

        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
            zsetKey,
            Double.NEGATIVE_INFINITY,
            cursorScore,
            0,
            fetchSize
        );
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
