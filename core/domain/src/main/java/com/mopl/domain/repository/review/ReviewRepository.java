package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    Optional<ReviewModel> findById(UUID reviewId);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    ReviewModel save(ReviewModel reviewModel);

    // cleanup batch 전용
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteByIdIn(List<UUID> reviewIds);
}
