package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    ReviewModel save(ReviewModel reviewModel);

    Optional<ReviewModel> findById(UUID reviewId);

    // 이하 메서드들 cleanup batch 전용
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteAllByIds(List<UUID> reviewIds);

    int softDeleteByContentIds(List<UUID> contentIds, Instant now);
}
