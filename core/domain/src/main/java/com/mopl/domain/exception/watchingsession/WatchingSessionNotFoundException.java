package com.mopl.domain.exception.watchingsession;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class WatchingSessionNotFoundException extends WatchingSessionException {

    private static final ErrorCode ERROR_CODE = WatchingSessionErrorCode.WATCHING_SESSION_NOT_FOUND;

    private WatchingSessionNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static WatchingSessionNotFoundException withUserIdAndContentId(UUID userId, UUID contentId) {
        return new WatchingSessionNotFoundException(Map.of("userId", userId, "contentId", contentId));
    }
}
