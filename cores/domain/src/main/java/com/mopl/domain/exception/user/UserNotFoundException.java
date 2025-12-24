package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

    public static final String MESSAGE = "해당 ID의 사용자를 찾을 수 없습니다.";

    public UserNotFoundException(UUID id) {
        super(MESSAGE, Map.of("id", id));
    }
}
