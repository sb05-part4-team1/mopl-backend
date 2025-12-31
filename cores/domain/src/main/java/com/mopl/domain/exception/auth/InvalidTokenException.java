package com.mopl.domain.exception.auth;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException() {
        super(AuthErrorCode.INVALID_TOKEN);
    }
}
