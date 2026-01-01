package com.mopl.domain.exception.auth;

public class AccountLockedException extends AuthException {

    private static final String MESSAGE = "계정이 잠금 상태입니다.";

    public AccountLockedException() {
        super(MESSAGE);
    }
}
