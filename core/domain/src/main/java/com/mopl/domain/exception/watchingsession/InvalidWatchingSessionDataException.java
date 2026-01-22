package com.mopl.domain.exception.watchingsession;

import java.util.Map;

public class InvalidWatchingSessionDataException extends WatchingSessionException {

    public InvalidWatchingSessionDataException(String detailMessage) {
        super(
            WatchingSessionErrorCode.INVALID_WATCHING_SESSION_DATA,
            Map.of("detailMessage", detailMessage)
        );
    }
}
