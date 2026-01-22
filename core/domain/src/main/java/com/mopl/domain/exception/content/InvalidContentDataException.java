package com.mopl.domain.exception.content;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidContentDataException extends ContentException {

    private static final ErrorCode ERROR_CODE = ContentErrorCode.INVALID_CONTENT_DATA;

    public InvalidContentDataException(String detailMessage) {
        super(ERROR_CODE, Map.of("detailMessage", detailMessage));
    }
}
