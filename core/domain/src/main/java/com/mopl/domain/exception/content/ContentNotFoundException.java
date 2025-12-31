package com.mopl.domain.exception.content;

import java.util.Map;

public class ContentNotFoundException extends ContentException {

    public ContentNotFoundException(String detailMessage) {
        super(ContentErrorCode.CONTENT_NOT_FOUND, Map.of("detailMessage", detailMessage));
    }
}
