package com.mopl.domain.exception.user;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

    public static final String MESSAGE = "해당 ID의 사용자를 찾을 수 없습니다.";

    private UserNotFoundException(String message, Map<String, Object> details) {
        super(message, details);
    }

    public static UserNotFoundException withId(UUID id) {
        return new UserNotFoundException(MESSAGE, Map.of("id", id));
    }

    public static UserNotFoundException withEmail(String email) {
        return new UserNotFoundException(MESSAGE, Map.of("email", email));
    }
}
