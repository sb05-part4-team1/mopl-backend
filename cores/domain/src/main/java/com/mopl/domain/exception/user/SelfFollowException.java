package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class SelfFollowException extends FollowException {

    public SelfFollowException(UUID id) {
        super(UserErrorCode.SELF_FOLLOW_NOT_ALLOWED, Map.of("id", id));
    }
}
