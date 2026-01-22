package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;

public class InvalidTokenException extends AuthException {

    private static final ErrorCode ERROR_CODE = AuthErrorCode.INVALID_TOKEN;

    private InvalidTokenException() {
        super(ERROR_CODE);
    }

    public static InvalidTokenException create() {
        return new InvalidTokenException();
    }
}
