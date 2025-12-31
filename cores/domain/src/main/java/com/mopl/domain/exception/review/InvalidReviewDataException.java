package com.mopl.domain.exception.review;

import java.util.Map;

public class InvalidReviewDataException extends ReviewException {

    public InvalidReviewDataException(String detailMessage) {
        super(ReviewErrorCode.INVALID_REVIEW_DATA, Map.of("detailMessage", detailMessage));
    }
}
