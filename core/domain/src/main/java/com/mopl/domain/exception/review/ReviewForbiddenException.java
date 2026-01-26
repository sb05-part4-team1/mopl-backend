package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReviewForbiddenException extends ReviewException {

    private static final ErrorCode ERROR_CODE = ReviewErrorCode.REVIEW_FORBIDDEN;

    private ReviewForbiddenException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ReviewForbiddenException withReviewIdAndRequesterIdAndAuthorId(
        UUID reviewId,
        UUID requesterId,
        UUID authorId
    ) {
        return new ReviewForbiddenException(Map.of(
            "reviewId", reviewId,
            "requesterId", requesterId,
            "authorId", authorId
        ));
    }
}
