package com.mopl.domain.exception.follow;

import java.util.Map;
import java.util.UUID;

public class FollowNotAllowedException extends FollowException {

    public FollowNotAllowedException(UUID userId, UUID followId) {
        super(FollowErrorCode.FOLLOW_NOT_ALLOWED, Map.of("userId", userId, "followId", followId));
    }
}
