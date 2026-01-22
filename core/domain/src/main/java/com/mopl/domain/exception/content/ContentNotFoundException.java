package com.mopl.domain.exception.content;

import java.util.Map;
import java.util.UUID;

public class ContentNotFoundException extends ContentException {

    private ContentNotFoundException(Map<String, Object> details) {
        super(ContentErrorCode.CONTENT_NOT_FOUND, details);
    }

    public static ContentNotFoundException withId(UUID id) {
        return new ContentNotFoundException(Map.of("id", id));
    }
}
