package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistContentAlreadyExistsException extends PlaylistException {

    public PlaylistContentAlreadyExistsException(
        UUID playlistId,
        UUID contentId
    ) {
        super(
            PlaylistErrorCode.PLAYLIST_CONTENT_ALREADY_EXISTS,
            Map.of(
                "playlistId", playlistId,
                "contentId", contentId
            )
        );
    }
}
