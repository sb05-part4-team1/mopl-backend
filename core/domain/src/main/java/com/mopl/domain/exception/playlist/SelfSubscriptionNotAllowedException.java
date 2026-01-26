package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SelfSubscriptionNotAllowedException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.SELF_SUBSCRIPTION_NOT_ALLOWED;

    private SelfSubscriptionNotAllowedException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static SelfSubscriptionNotAllowedException withPlaylistIdAndUserId(UUID playlistId, UUID userId) {
        return new SelfSubscriptionNotAllowedException(Map.of("playlistId", playlistId, "userId", userId));
    }
}
