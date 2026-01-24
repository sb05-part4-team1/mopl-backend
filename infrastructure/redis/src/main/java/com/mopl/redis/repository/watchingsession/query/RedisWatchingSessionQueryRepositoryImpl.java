package com.mopl.redis.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;
import com.mopl.redis.support.cursor.RedisCursorPaginationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class RedisWatchingSessionQueryRepositoryImpl implements WatchingSessionQueryRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public CursorResponse<WatchingSessionModel> findByContentId(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        String zsetKey = WatchingSessionRedisKeys.contentSessionsZsetKey(contentId);
        WatchingSessionSortFieldSupport sortField = WatchingSessionSortFieldSupport.from(
            request.sortBy());

        long totalCount = countTotal(zsetKey, request.watcherNameLike());

        int limit = request.limit();
        int fetchSize = Math.min(limit * 5, 2000);

        List<WatchingSessionModel> candidates = fetchCandidates(zsetKey, request, sortField,
            fetchSize);

        List<WatchingSessionModel> afterCursor = RedisCursorPaginationHelper.applyCursor(
            candidates,
            request,
            sortField,
            sortField::extractValue,
            WatchingSessionModel::getId
        );

        return RedisCursorPaginationHelper.buildResponse(
            afterCursor,
            request,
            sortField,
            totalCount,
            sortField::extractValue,
            WatchingSessionModel::getId
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

        List<WatchingSessionModel> models = new ArrayList<>(tuples.size());

        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null) {
                continue;
            }

            String sessionIdStr = tuple.getValue().toString();
            UUID sessionId = parseUuid(sessionIdStr);
            if (sessionId == null) {
                continue;
            }

            Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(
                sessionId));
            if (stored instanceof WatchingSessionModel model) {
                if (matchesWatcherNameFilter(model, request.watcherNameLike())) {
                    models.add(model);
                }
            }
        }

        return models.stream()
            .sorted((a, b) -> RedisCursorPaginationHelper.compareByFieldThenId(
                a, b,
                sortField::extractValue,
                WatchingSessionModel::getId,
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
            cursorScore,
            Double.NEGATIVE_INFINITY,
            0,
            fetchSize
        );
    }

    private long countTotal(String zsetKey, String watcherNameLike) {
        if (!StringUtils.hasText(watcherNameLike) || !StringUtils.hasText(watcherNameLike.trim())) {
            Long size = redisTemplate.opsForZSet().size(zsetKey);
            return size != null ? size : 0L;
        }

        Set<Object> allSessionIds = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
        if (allSessionIds == null || allSessionIds.isEmpty()) {
            return 0L;
        }

        long count = 0;
        for (Object obj : allSessionIds) {
            if (obj == null) {
                continue;
            }

            UUID sessionId = parseUuid(obj.toString());
            if (sessionId == null) {
                continue;
            }

            Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(
                sessionId));
            if (stored instanceof WatchingSessionModel model) {
                if (matchesWatcherNameFilter(model, watcherNameLike)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean matchesWatcherNameFilter(WatchingSessionModel session, String watcherNameLike) {
        if (!hasText(watcherNameLike)) {
            return true;
        }

        String keyword = watcherNameLike.trim();
        if (!hasText(keyword)) {
            return true;
        }

        if (session.getWatcher() == null || !hasText(session.getWatcher().getName())) {
            return false;
        }

        return session.getWatcher().getName().toLowerCase().contains(keyword.toLowerCase());
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
