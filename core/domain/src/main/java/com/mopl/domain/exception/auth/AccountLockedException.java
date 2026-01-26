package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class AccountLockedException extends AuthException {

    private static final ErrorCode ERROR_CODE = AuthErrorCode.ACCOUNT_LOCKED;

    private AccountLockedException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static AccountLockedException withId(UUID id) {
        return new AccountLockedException(Map.of("id", id));
    }

    public static AccountLockedException withEmail(String email) {
        return new AccountLockedException(Map.of("email", email));
    }
}
