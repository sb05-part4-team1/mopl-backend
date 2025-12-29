package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class SelfFollowException extends FollowException {

    public static final String MESSAGE = "자기 자신을 팔로우할 수 없습니다.";

    public SelfFollowException(UUID id) {
        super(MESSAGE, Map.of("id", id));
    }
}
