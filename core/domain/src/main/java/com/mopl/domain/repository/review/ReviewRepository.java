package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    ReviewModel save(ReviewModel reviewModel);

    Optional<ReviewModel> findById(UUID reviewId);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteByIdIn(List<UUID> reviewIds);

    int softDeleteByContentIdIn(List<UUID> contentIds, Instant now);
}
