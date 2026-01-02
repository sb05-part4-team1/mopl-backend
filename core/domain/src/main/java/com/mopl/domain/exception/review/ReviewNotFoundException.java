package com.mopl.domain.exception.review;

import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends ReviewException {

    public static final String MESSAGE = "리뷰를 찾을 수 없습니다.";

    public ReviewNotFoundException(UUID reviewId) {
        super(MESSAGE, Map.of(
            "reviewId", reviewId
        ));
    }
}
