package com.mopl.jpa.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;
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

        // 1) totalCount 계산
        long totalCount = countTotal(zsetKey, request.watcherNameLike());

        // 2) 정렬 방향에 맞게 “후보 세션”을 넉넉히 가져온다 (watcherNameLike 필터 때문에 buffer 필요)
        int limit = request.limit();
        int fetchSize = Math.min(limit * 5, 2000); // 너무 많이 가져오지 않게 상한

        List<WatchingSessionModel> candidates = fetchCandidates(zsetKey, request, fetchSize);

        // 3) 커서 조건(프로젝트 표준: cursor + idAfter 둘 다 있어야 적용)을 애플리케이션에서 한번 더 정확히 적용
        List<WatchingSessionModel> afterCursor = applyCursor(candidates, request);

        // 4) limit+1로 hasNext 판단
        List<WatchingSessionModel> pagePlusOne = afterCursor.stream()
                .limit((long) limit + 1)
                .toList();

        boolean hasNext = pagePlusOne.size() > limit;
        List<WatchingSessionModel> result = hasNext ? pagePlusOne.subList(0, limit) : pagePlusOne;

        if (result.isEmpty()) {
            return CursorResponse.empty("createdAt", request.sortDirection());
        }

        if (!hasNext) {
            return CursorResponse.of(
                    result,
                    null,
                    null,
                    false,
                    totalCount,
                    "createdAt",
                    request.sortDirection()
            );
        }

        WatchingSessionModel last = result.get(result.size() - 1);
        String nextCursor = last.getCreatedAt() != null ? last.getCreatedAt().toString() : null;
        UUID nextIdAfter = last.getId();

        return CursorResponse.of(
                result,
                nextCursor,
                nextIdAfter,
                true,
                totalCount,
                "createdAt",
                request.sortDirection()
        );
    }

    private List<WatchingSessionModel> fetchCandidates(
            String zsetKey,
            WatchingSessionQueryRequest request,
            int fetchSize
    ) {
        SortDirection direction = request.sortDirection();

        // cursor가 있으면 createdAt 기반으로 score 범위 조회를 시도하고,
        // 없으면 앞에서부터 range/revRange로 가져온다.
        Instant cursorInstant = parseInstant(request.cursor());

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

            Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(sessionId));
            if (stored instanceof WatchingSessionModel model) {
                // watcherNameLike 필터는 Redis에서 못하니 여기서 처리
                if (watcherNameLike(model, request.watcherNameLike())) {
                    models.add(model);
                }
            }
        }

        // 혹시 Redis 정렬/조회 특성상 순서가 흔들릴 여지가 있어 createdAt + id로 한번 더 정렬해줌
        return models.stream()
                .sorted((a, b) -> compareByCreatedAtThenId(a, b, direction))
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
            // cursorScore 이상을 가져와서 idAfter tie-break는 applyCursor에서 처리
            return redisTemplate.opsForZSet().rangeByScoreWithScores(
                    zsetKey,
                    cursorScore,
                    Double.POSITIVE_INFINITY,
                    0,
                    fetchSize
            );
        }

        // DESC: cursorScore 이하를 역순으로 가져옴
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                zsetKey,
                cursorScore,
                Double.NEGATIVE_INFINITY,
                0,
                fetchSize
        );
    }

    private List<WatchingSessionModel> applyCursor(
            List<WatchingSessionModel> sorted,
            WatchingSessionQueryRequest request
    ) {
        // 프로젝트 표준: cursor + idAfter 둘 다 있어야 커서 조건 적용
        if (request.idAfter() == null || !hasText(request.cursor())) {
            return sorted;
        }

        Instant cursorInstant = parseInstant(request.cursor());
        if (cursorInstant == null) {
            return sorted;
        }

        UUID idAfter = request.idAfter();
        boolean isAscending = request.sortDirection().isAscending();

        return sorted.stream()
                .filter(session -> {
                    if (session == null || session.getId() == null || session.getCreatedAt() == null) {
                        return false;
                    }

                    int cmp = session.getCreatedAt().compareTo(cursorInstant);

                    if (isAscending) {
                        return (cmp > 0) || (cmp == 0 && session.getId().compareTo(idAfter) > 0);
                    }
                    return (cmp < 0) || (cmp == 0 && session.getId().compareTo(idAfter) < 0);
                })
                .toList();
    }

    private long countTotal(String zsetKey, String watcherNameLike) {
        // watcherNameLike 없으면 ZCARD로 끝
        if (!StringUtils.hasText(watcherNameLike) || !StringUtils.hasText(watcherNameLike.trim())) {
            Long size = redisTemplate.opsForZSet().size(zsetKey);
            return size != null ? size : 0L;
        }

        // watcherNameLike 있으면 정확한 totalCount를 위해 전체를 스캔(데이터가 커지면 최적화 필요)
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

            Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(sessionId));
            if (stored instanceof WatchingSessionModel model) {
                if (watcherNameLike(model, watcherNameLike)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean watcherNameLike(WatchingSessionModel session, String watcherNameLike) {
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

    private int compareByCreatedAtThenId(
            WatchingSessionModel a,
            WatchingSessionModel b,
            SortDirection direction
    ) {
        Instant aCreated = a != null ? a.getCreatedAt() : null;
        Instant bCreated = b != null ? b.getCreatedAt() : null;

        int cmpCreated;
        if (aCreated == null && bCreated == null) {
            cmpCreated = 0;
        } else if (aCreated == null) {
            cmpCreated = 1;
        } else if (bCreated == null) {
            cmpCreated = -1;
        } else {
            cmpCreated = aCreated.compareTo(bCreated);
        }

        UUID aId = a != null ? a.getId() : null;
        UUID bId = b != null ? b.getId() : null;

        int cmpId;
        if (aId == null && bId == null) {
            cmpId = 0;
        } else if (aId == null) {
            cmpId = 1;
        } else if (bId == null) {
            cmpId = -1;
        } else {
            cmpId = aId.compareTo(bId);
        }

        int result = (cmpCreated != 0) ? cmpCreated : cmpId;

        return direction.isAscending() ? result : -result;
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

    private Instant parseInstant(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }
        try {
            return Instant.parse(cursor.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
