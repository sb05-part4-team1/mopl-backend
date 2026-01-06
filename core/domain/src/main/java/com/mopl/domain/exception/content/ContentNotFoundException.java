package com.mopl.domain.exception.content;

import java.util.Map;
import java.util.UUID;

public class ContentNotFoundException extends ContentException {

    private ContentNotFoundException(Map<String, Object> details) {
        super(ContentErrorCode.CONTENT_NOT_FOUND, details);
    }

    public static ContentNotFoundException withId(UUID id) {
        return new ContentNotFoundException(Map.of(
            "contentId", id,
            "detailMessage", "해당 ID의 콘텐츠를 찾을 수 없습니다."
        ));
    }
}
