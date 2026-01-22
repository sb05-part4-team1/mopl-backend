package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidReviewDataException extends ReviewException {

    private static final ErrorCode ERROR_CODE = ReviewErrorCode.INVALID_REVIEW_DATA;

    private InvalidReviewDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidReviewDataException withDetailMessage(String detailMessage) {
        return new InvalidReviewDataException(Map.of("detailMessage", detailMessage));
    }
}
