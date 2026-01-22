package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;

public class InvalidCredentialsException extends AuthException {

    private static final ErrorCode ERROR_CODE = AuthErrorCode.INVALID_CREDENTIALS;

    private InvalidCredentialsException() {
        super(ERROR_CODE);
    }

    public static InvalidCredentialsException create() {
        return new InvalidCredentialsException();
    }
}
