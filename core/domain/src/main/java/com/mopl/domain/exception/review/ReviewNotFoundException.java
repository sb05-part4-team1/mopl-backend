package com.mopl.domain.exception.review;

import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends ReviewException {

    public ReviewNotFoundException(UUID id) {
        super(ReviewErrorCode.REVIEW_NOT_FOUND, Map.of("id", id));
    }
}
