package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;

public class UnauthorizedException extends AuthException {

    private static final ErrorCode ERROR_CODE = AuthErrorCode.UNAUTHORIZED;

    private UnauthorizedException() {
        super(ERROR_CODE);
    }

    public static UnauthorizedException create() {
        return new UnauthorizedException();
    }
}
