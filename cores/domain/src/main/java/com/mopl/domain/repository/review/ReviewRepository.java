package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;

public interface ReviewRepository {

    ReviewModel save(ReviewModel reviewModel);
}
