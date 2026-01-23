package com.mopl.redis.repository.review;

import com.mopl.domain.model.review.ReviewStats;
import com.mopl.domain.repository.review.ReviewStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisReviewStatsRepository implements ReviewStatsRepository {

    private static final String KEY_PREFIX = "content:review:stats:";
    private static final String FIELD_AVG = "avg";
    private static final String FIELD_COUNT = "count";
    private static final Duration TTL = Duration.ofHours(48);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public ReviewStats getStats(UUID contentId) {
        String key = buildKey(contentId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return null;
        }

        return parseStats(entries);
    }

    @Override
    public Map<UUID, ReviewStats> getStats(Collection<UUID> contentIds) {
        if (contentIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, ReviewStats> result = new HashMap<>();
        for (UUID contentId : contentIds) {
            ReviewStats stats = getStats(contentId);
            if (stats != null) {
                result.put(contentId, stats);
            }
        }
        return result;
    }

    @Override
    public void setStats(UUID contentId, ReviewStats stats) {
        String key = buildKey(contentId);
        Map<String, Object> entries = Map.of(
            FIELD_AVG, stats.averageRating(),
            FIELD_COUNT, stats.reviewCount()
        );
        redisTemplate.opsForHash().putAll(key, entries);
        redisTemplate.expire(key, TTL);
    }

    @Override
    public void applyReview(UUID contentId, double rating) {
        ReviewStats current = getStats(contentId);
        if (current == null) {
            return;
        }

        int newCount = current.reviewCount() + 1;
        double newAverage = ((current.averageRating() * current.reviewCount()) + rating) / newCount;
        setStats(contentId, new ReviewStats(newAverage, newCount));
    }

    @Override
    public void updateReview(UUID contentId, double oldRating, double newRating) {
        ReviewStats current = getStats(contentId);
        if (current == null || current.reviewCount() == 0) {
            return;
        }

        double total = current.averageRating() * current.reviewCount();
        double newAverage = (total - oldRating + newRating) / current.reviewCount();
        setStats(contentId, new ReviewStats(newAverage, current.reviewCount()));
    }

    @Override
    public void removeReview(UUID contentId, double rating) {
        ReviewStats current = getStats(contentId);
        if (current == null || current.reviewCount() == 0) {
            return;
        }

        int newCount = current.reviewCount() - 1;
        if (newCount <= 0) {
            setStats(contentId, ReviewStats.empty());
            return;
        }

        double total = current.averageRating() * current.reviewCount();
        double newAverage = (total - rating) / newCount;
        setStats(contentId, new ReviewStats(newAverage, newCount));
    }

    private ReviewStats parseStats(Map<Object, Object> entries) {
        Object avgValue = entries.get(FIELD_AVG);
        Object countValue = entries.get(FIELD_COUNT);

        double avg = avgValue != null ? parseDouble(avgValue) : 0.0;
        int count = countValue != null ? parseInt(countValue) : 0;

        return new ReviewStats(avg, count);
    }

    private double parseDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private int parseInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private String buildKey(UUID contentId) {
        return KEY_PREFIX + contentId.toString();
    }
}
