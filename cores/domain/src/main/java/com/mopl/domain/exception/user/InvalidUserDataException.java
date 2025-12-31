package com.mopl.domain.exception.user;

import java.util.Map;

public class InvalidUserDataException extends UserException {

    public InvalidUserDataException(String detailMessage) {
        super(UserErrorCode.INVALID_USER_DATA, Map.of("detailMessage", detailMessage));
    }
}
