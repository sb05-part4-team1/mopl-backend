package com.mopl.domain.exception.content;

import java.util.Map;

public class ContentNotFoundException extends ContentException {

    public static final String MESSAGE = "해당 ID의 콘텐츠를 찾을 수 없습니다.";

    public ContentNotFoundException(String detailMessage) {
        super(MESSAGE, Map.of("detailMessage", detailMessage));
    }
}
