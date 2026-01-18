package com.mopl.domain.repository.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.support.cursor.CursorResponse;

public interface ReviewQueryRepository {

    CursorResponse<ReviewModel> findAll(ReviewQueryRequest request);
}
