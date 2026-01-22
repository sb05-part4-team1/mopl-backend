package com.mopl.domain.exception.tag;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidTagDataException extends TagException {

    private static final ErrorCode ERROR_CODE = TagErrorCode.INVALID_TAG_DATA;

    public InvalidTagDataException(String detailMessage) {
        super(ERROR_CODE, Map.of("detailMessage", detailMessage));
    }
}
