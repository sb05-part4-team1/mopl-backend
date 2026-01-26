package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class FollowAlreadyExistsException extends FollowException {

    private static final ErrorCode ERROR_CODE = FollowErrorCode.FOLLOW_ALREADY_EXISTS;

    private FollowAlreadyExistsException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FollowAlreadyExistsException withIds(UUID followerId, UUID followeeId) {
        return new FollowAlreadyExistsException(Map.of(
            "followerId", followerId,
            "followeeId", followeeId
        ));
    }
}
