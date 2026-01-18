package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistSubscriptionAlreadyExistsException extends PlaylistException {

    public PlaylistSubscriptionAlreadyExistsException(UUID playlistId, UUID subscriberId) {
        super(
            PlaylistErrorCode.PLAYLIST_SUBSCRIPTION_ALREADY_EXISTS,
            Map.of(
                "playlistId", playlistId,
                "subscriberId", subscriberId
            )
        );
    }
}
