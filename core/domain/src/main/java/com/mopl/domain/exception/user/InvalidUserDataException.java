package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidUserDataException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.INVALID_USER_DATA;

    private InvalidUserDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidUserDataException withDetailMessage(String detailMessage) {
        return new InvalidUserDataException(Map.of("detailMessage", detailMessage));
    }
}
