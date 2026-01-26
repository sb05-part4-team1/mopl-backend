package com.mopl.domain.exception.watchingsession;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidWatchingSessionDataException extends WatchingSessionException {

    private static final ErrorCode ERROR_CODE = WatchingSessionErrorCode.INVALID_WATCHING_SESSION_DATA;

    private InvalidWatchingSessionDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidWatchingSessionDataException withDetailMessage(String detailMessage) {
        return new InvalidWatchingSessionDataException(Map.of("detailMessage", detailMessage));
    }
}
