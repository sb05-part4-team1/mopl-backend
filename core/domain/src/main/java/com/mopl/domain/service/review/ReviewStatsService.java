package com.mopl.domain.service.review;

import com.mopl.domain.model.review.ReviewStats;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.repository.review.ReviewStatsRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ReviewStatsService {

    private final ReviewStatsRepository reviewStatsRepository;
    private final ReviewRepository reviewRepository;

    public ReviewStats getStats(UUID contentId) {
        try {
            ReviewStats stats = reviewStatsRepository.getStats(contentId);
            if (stats != null) {
                return stats;
            }
        } catch (Exception ignored) {
            // Redis 장애 시 DB fallback
        }
        return reviewRepository.getStatsByContentId(contentId);
    }

    public Map<UUID, ReviewStats> getStats(Collection<UUID> contentIds) {
        try {
            Map<UUID, ReviewStats> stats = reviewStatsRepository.getStats(contentIds);
            if (!stats.isEmpty()) {
                return stats;
            }
        } catch (Exception ignored) {
            // Redis 장애 시 DB fallback
        }

        Map<UUID, ReviewStats> result = new HashMap<>();
        for (UUID contentId : contentIds) {
            result.put(contentId, reviewRepository.getStatsByContentId(contentId));
        }
        return result;
    }

    public void applyReview(UUID contentId, double rating) {
        try {
            reviewStatsRepository.applyReview(contentId, rating);
        } catch (Exception ignored) {
            // Redis 장애 시 무시, sync scheduler가 보정
        }
    }

    public void updateReview(UUID contentId, double oldRating, double newRating) {
        try {
            reviewStatsRepository.updateReview(contentId, oldRating, newRating);
        } catch (Exception ignored) {
            // Redis 장애 시 무시, sync scheduler가 보정
        }
    }

    public void removeReview(UUID contentId, double rating) {
        try {
            reviewStatsRepository.removeReview(contentId, rating);
        } catch (Exception ignored) {
            // Redis 장애 시 무시, sync scheduler가 보정
        }
    }

    public void setStats(UUID contentId, ReviewStats stats) {
        try {
            reviewStatsRepository.setStats(contentId, stats);
        } catch (Exception ignored) {
            // Redis 장애 시 무시
        }
    }
}
