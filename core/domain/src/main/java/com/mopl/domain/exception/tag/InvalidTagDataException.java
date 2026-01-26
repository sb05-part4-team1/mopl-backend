package com.mopl.domain.exception.tag;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidTagDataException extends TagException {

    private static final ErrorCode ERROR_CODE = TagErrorCode.INVALID_TAG_DATA;

    private InvalidTagDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidTagDataException withDetailMessage(String detailMessage) {
        return new InvalidTagDataException(Map.of("detailMessage", detailMessage));
    }
}
