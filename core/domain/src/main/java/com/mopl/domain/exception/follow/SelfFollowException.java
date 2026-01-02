package com.mopl.domain.exception.follow;

import java.util.Map;
import java.util.UUID;

public class SelfFollowException extends FollowException {

    public SelfFollowException(UUID id) {
        super(FollowErrorCode.SELF_FOLLOW_NOT_ALLOWED, Map.of("id", id));
    }
}
