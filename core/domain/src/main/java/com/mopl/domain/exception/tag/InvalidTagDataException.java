package com.mopl.domain.exception.tag;

import java.util.Map;

public class InvalidTagDataException extends TagException {

    public InvalidTagDataException(String detailMessage) {
        super(TagErrorCode.INVALID_TAG_DATA, Map.of("detailMessage", detailMessage));
    }
}
