package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;

import java.util.HashMap;
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
        Map<String, Object> details = new HashMap<>();
        details.put("reviewId", reviewId);
        details.put("requesterId", requesterId);
        details.put("authorId", authorId);
        return new ReviewForbiddenException(details);
    }
}
