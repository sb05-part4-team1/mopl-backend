package com.mopl.domain.exception.review;

import java.util.Map;

public class InvalidReviewDataException extends ReviewException {

    public static final String MESSAGE = "리뷰 데이터가 유효하지 않습니다.";

    public InvalidReviewDataException(String detailMessage) {
        super(MESSAGE, Map.of("detailMessage", detailMessage));
    }
}
