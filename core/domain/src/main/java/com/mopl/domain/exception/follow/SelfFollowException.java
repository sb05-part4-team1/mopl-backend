package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SelfFollowException extends FollowException {

    private static final ErrorCode ERROR_CODE = FollowErrorCode.SELF_FOLLOW_NOT_ALLOWED;

    private SelfFollowException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static SelfFollowException withUserId(UUID userId) {
        return new SelfFollowException(Map.of("userId", userId));
    }
}
