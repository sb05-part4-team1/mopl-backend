package com.mopl.domain.exception.auth;

public class InsufficientRoleException extends AuthException {

    private static final String MESSAGE = "해당 작업을 수행할 권한이 없습니다.";

    public InsufficientRoleException() {
        super(MESSAGE);
    }
}
