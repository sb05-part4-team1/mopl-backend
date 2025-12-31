package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN(401, "유효하지 않은 토큰입니다"),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다"),
    UNAUTHORIZED(401, "로그인이 필요합니다"),
    ACCOUNT_LOCKED(403, "계정이 잠금 상태입니다"),
    INSUFFICIENT_ROLE(403, "해당 작업을 수행할 권한이 없습니다");

    private final int status;
    private final String message;
}
