package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewStats;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ReviewStatsRepository {

    ReviewStats getStats(UUID contentId);

    Map<UUID, ReviewStats> getStats(Collection<UUID> contentIds);

    void setStats(UUID contentId, ReviewStats stats);

    void applyReview(UUID contentId, double rating);

    void updateReview(UUID contentId, double oldRating, double newRating);

    void removeReview(UUID contentId, double rating);
}
