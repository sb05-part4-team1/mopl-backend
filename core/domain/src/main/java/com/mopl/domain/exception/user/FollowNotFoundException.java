package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class FollowNotFoundException extends FollowException {

    public static final String MESSAGE = "존재하지 않거나 이미 취소된 팔로우 관계입니다.";

    public FollowNotFoundException(UUID followId) {
        super(MESSAGE, Map.of("followId", followId));
    }
}
