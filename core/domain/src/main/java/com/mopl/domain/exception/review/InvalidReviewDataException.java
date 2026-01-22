package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidReviewDataException extends ReviewException {

    private static final ErrorCode ERROR_CODE = ReviewErrorCode.INVALID_REVIEW_DATA;

    public InvalidReviewDataException(String detailMessage) {
        super(ERROR_CODE, Map.of("detailMessage", detailMessage));
    }
}
