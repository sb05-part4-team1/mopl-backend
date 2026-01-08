package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SelfLockChangeException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.SELF_LOCK_CHANGE_NOT_ALLOWED;

    private SelfLockChangeException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static SelfLockChangeException withUserId(UUID userId) {
        return new SelfLockChangeException(Map.of("userId", userId));
    }
}
