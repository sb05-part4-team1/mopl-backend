package com.mopl.domain.exception.follow;

import java.util.Map;
import java.util.UUID;

public class FollowNotFoundException extends FollowException {

    public FollowNotFoundException(UUID id) {
        super(FollowErrorCode.FOLLOW_NOT_FOUND, Map.of("id", id));
    }
}
