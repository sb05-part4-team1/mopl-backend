package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    Optional<ReviewModel> findById(UUID reviewId);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    ReviewModel save(ReviewModel reviewModel);

    void delete(UUID reviewId);
}
