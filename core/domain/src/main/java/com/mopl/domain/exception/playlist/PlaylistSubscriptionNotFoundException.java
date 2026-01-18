package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistSubscriptionNotFoundException extends PlaylistException {

    public PlaylistSubscriptionNotFoundException(UUID playlistId, UUID subscriberId) {
        super(
            PlaylistErrorCode.PLAYLIST_SUBSCRIPTION_NOT_FOUND,
            Map.of(
                "playlistId", playlistId,
                "subscriberId", subscriberId
            )
        );
    }
}
