package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class FollowNotFoundException extends FollowException {

    private static final ErrorCode ERROR_CODE = FollowErrorCode.FOLLOW_NOT_FOUND;

    private FollowNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FollowNotFoundException withId(UUID id) {
        return new FollowNotFoundException(Map.of("id", id));
    }
}
