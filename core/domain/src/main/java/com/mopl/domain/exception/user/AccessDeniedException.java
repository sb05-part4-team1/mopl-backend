package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class AccessDeniedException extends FollowException {

    public static final String MESSAGE = "본인의 팔로우만 취소할 수 있습니다.";

    public AccessDeniedException(UUID userId, UUID followId) {
        super(MESSAGE, Map.of("userId", userId, "followId", followId));
    }
}
