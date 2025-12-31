package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.USER_NOT_FOUND;

    private UserNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static UserNotFoundException withId(UUID id) {
        return new UserNotFoundException(Map.of("id", id));
    }

    public static UserNotFoundException withEmail(String email) {
        return new UserNotFoundException(Map.of("email", email));
    }
}
