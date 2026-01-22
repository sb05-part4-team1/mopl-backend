package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.user.UserErrorCode;
import com.mopl.domain.exception.user.UserNotFoundException;

import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends ReviewException {

    private static final ErrorCode ERROR_CODE = ReviewErrorCode.REVIEW_NOT_FOUND;

    private ReviewNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ReviewNotFoundException withId(UUID id) {
        return new ReviewNotFoundException(Map.of("id", id));
    }
}
