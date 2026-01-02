package com.mopl.domain.exception.review;

import java.util.Map;
import java.util.UUID;

public class ReviewForbiddenException extends ReviewException {

    public ReviewForbiddenException(
        UUID reviewId,
        UUID requesterId,
        UUID authorId
    ) {
        super(ReviewErrorCode.REVIEW_FORBIDDEN, Map.of(
            "reviewId", reviewId,
            "requesterId", requesterId,
            "authorId", authorId
        ));
    }
}
