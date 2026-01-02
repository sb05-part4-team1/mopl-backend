package com.mopl.domain.exception.review;

import java.util.Map;
import java.util.UUID;

public class ReviewForbiddenException extends ReviewException {

    public static final String MESSEGE = "리뷰를 수정할 권한이 없습니다.";

    public ReviewForbiddenException(
        UUID reviewId,
        UUID requesterId,
        UUID authorId
    ) {
        super(MESSEGE, Map.of(
            "reviewId", reviewId,
            "requesterId", requesterId,
            "authorId", authorId
        ));
    }
}
