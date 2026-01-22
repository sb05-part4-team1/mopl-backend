package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;

public class InsufficientRoleException extends AuthException {

    private static final ErrorCode ERROR_CODE = AuthErrorCode.INSUFFICIENT_ROLE;

    private InsufficientRoleException() {
        super(ERROR_CODE);
    }

    public static InsufficientRoleException create() {
        return new InsufficientRoleException();
    }
}
