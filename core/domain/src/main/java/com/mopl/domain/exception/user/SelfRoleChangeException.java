package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SelfRoleChangeException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.SELF_ROLE_CHANGE_NOT_ALLOWED;

    private SelfRoleChangeException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static SelfRoleChangeException withUserId(UUID userId) {
        return new SelfRoleChangeException(Map.of("userId", userId));
    }
}
