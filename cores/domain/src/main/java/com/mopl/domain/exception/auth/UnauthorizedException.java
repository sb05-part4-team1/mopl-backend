package com.mopl.domain.exception.auth;

public class UnauthorizedException extends AuthException {

    public UnauthorizedException() {
        super(AuthErrorCode.UNAUTHORIZED);
    }
}
