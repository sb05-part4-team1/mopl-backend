package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistContentNotFoundException extends PlaylistException {

    public PlaylistContentNotFoundException(UUID playlistId, UUID contentId) {
        super(
            PlaylistErrorCode.PLAYLIST_CONTENT_NOT_FOUND,
            Map.of(
                "playlistId", playlistId,
                "contentId", contentId
            )
        );
    }
}
