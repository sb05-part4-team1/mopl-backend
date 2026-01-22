package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class PlaylistSubscriptionNotFoundException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_SUBSCRIPTION_NOT_FOUND;

    private PlaylistSubscriptionNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistSubscriptionNotFoundException withPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        return new PlaylistSubscriptionNotFoundException(Map.of("playlistId", playlistId, "subscriberId", subscriberId));
    }
}
