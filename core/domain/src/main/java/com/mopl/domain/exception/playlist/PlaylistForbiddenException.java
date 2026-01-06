package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistForbiddenException extends PlaylistException {

    public PlaylistForbiddenException(
        UUID playlistId,
        UUID requesterId,
        UUID ownerId
    ) {
        super(
            PlaylistErrorCode.PLAYLIST_FORBIDDEN,
            Map.of(
                "playlistId", playlistId,
                "requesterId", requesterId,
                "ownerId", ownerId
            )
        );
    }
}
