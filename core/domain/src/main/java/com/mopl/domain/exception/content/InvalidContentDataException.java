package com.mopl.domain.exception.content;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidContentDataException extends ContentException {

    private static final ErrorCode ERROR_CODE = ContentErrorCode.INVALID_CONTENT_DATA;

    private InvalidContentDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidContentDataException withDetailMessage(String detailMessage) {
        return new InvalidContentDataException(Map.of("detailMessage", detailMessage));
    }
}
