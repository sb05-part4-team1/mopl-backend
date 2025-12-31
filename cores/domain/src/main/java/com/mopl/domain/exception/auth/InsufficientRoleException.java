package com.mopl.domain.exception.auth;

public class InsufficientRoleException extends AuthException {

    public InsufficientRoleException() {
        super(AuthErrorCode.INSUFFICIENT_ROLE);
    }
}
