package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReviewAlreadyExistsException extends ReviewException {

    private static final ErrorCode ERROR_CODE = ReviewErrorCode.REVIEW_ALREADY_EXISTS;

    private ReviewAlreadyExistsException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ReviewAlreadyExistsException withContentIdAndAuthorId(UUID contentId, UUID authorId) {
        return new ReviewAlreadyExistsException(Map.of(
            "contentId", contentId,
            "authorId", authorId
        ));
    }
}
