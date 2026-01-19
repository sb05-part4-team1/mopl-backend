package com.mopl.domain.exception.watchingsession;

import java.util.Map;
import java.util.UUID;

public class WatchingSessionNotFoundException extends WatchingSessionException {

    public WatchingSessionNotFoundException(UUID userId, UUID contentId) {
        super(WatchingSessionErrorCode.WATCHING_SESSION_NOT_FOUND, Map.of("userId", userId,
            "contentId", contentId));
    }
}
