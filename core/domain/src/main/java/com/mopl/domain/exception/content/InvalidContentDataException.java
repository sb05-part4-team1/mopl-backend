package com.mopl.domain.exception.content;

import java.util.Map;

public class InvalidContentDataException extends ContentException {

    public InvalidContentDataException(String detailMessage) {
        super(ContentErrorCode.INVALID_CONTENT_DATA, Map.of("detailMessage", detailMessage));
    }
}
