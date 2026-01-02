package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_USER_DATA(400, "유효하지 않은 사용자 데이터입니다"),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다");

    private final int status;
    private final String message;
}
