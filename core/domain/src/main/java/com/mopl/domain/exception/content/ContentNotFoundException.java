package com.mopl.domain.exception.content;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ContentNotFoundException extends ContentException {

    private static final ErrorCode ERROR_CODE = ContentErrorCode.CONTENT_NOT_FOUND;

    private ContentNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ContentNotFoundException withId(UUID id) {
        return new ContentNotFoundException(Map.of("id", id));
    }
}
