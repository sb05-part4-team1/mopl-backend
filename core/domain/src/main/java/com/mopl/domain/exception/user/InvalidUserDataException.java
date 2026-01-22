package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidUserDataException extends UserException {

    private static final ErrorCode ERROR_CODE = UserErrorCode.INVALID_USER_DATA;

    public InvalidUserDataException(String detailMessage) {
        super(ERROR_CODE, Map.of("detailMessage", detailMessage));
    }
}
