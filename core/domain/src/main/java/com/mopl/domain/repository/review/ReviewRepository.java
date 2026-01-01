package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    ReviewModel save(ReviewModel reviewModel);

    Optional<ReviewModel> findById(UUID reviewId);
}
