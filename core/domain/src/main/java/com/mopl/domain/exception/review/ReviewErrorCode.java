package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    INVALID_REVIEW_DATA(400, "유효하지 않은 리뷰 데이터입니다"),
    REVIEW_NOT_FOUND(404, "리뷰를 찾을 수 없습니다"),
    REVIEW_FORBIDDEN(403, "리뷰를 수정할 권한이 없습니다");

    private final int status;
    private final String message;
}
