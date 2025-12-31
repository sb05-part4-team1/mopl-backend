package com.mopl.domain.exception.auth;

public class AccountLockedException extends AuthException {

    public AccountLockedException() {
        super(AuthErrorCode.ACCOUNT_LOCKED);
    }
}
