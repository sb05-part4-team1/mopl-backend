package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class PlaylistSubscriptionAlreadyExistsException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_SUBSCRIPTION_ALREADY_EXISTS;

    private PlaylistSubscriptionAlreadyExistsException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistSubscriptionAlreadyExistsException withPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        return new PlaylistSubscriptionAlreadyExistsException(Map.of("playlistId", playlistId, "subscriberId", subscriberId));
    }
}
