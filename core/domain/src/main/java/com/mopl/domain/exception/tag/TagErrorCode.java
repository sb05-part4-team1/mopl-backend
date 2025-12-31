package com.mopl.domain.exception.tag;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagErrorCode implements ErrorCode {

    INVALID_TAG_DATA(400, "유효하지 않은 태그 데이터입니다");

    private final int status;
    private final String message;
}
