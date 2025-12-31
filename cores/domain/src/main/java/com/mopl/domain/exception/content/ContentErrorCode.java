package com.mopl.domain.exception.content;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentErrorCode implements ErrorCode {

    INVALID_CONTENT_DATA(400, "유효하지 않은 콘텐츠 데이터입니다"),
    CONTENT_NOT_FOUND(404, "콘텐츠를 찾을 수 없습니다");

    private final int status;
    private final String message;
}
