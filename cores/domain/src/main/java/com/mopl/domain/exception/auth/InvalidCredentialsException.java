package com.mopl.domain.exception.auth;

public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super(AuthErrorCode.INVALID_CREDENTIALS);
    }
}
