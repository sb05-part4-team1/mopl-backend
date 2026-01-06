package com.mopl.domain.exception.playlist;

import java.util.Map;

public class InvalidPlaylistDataException extends PlaylistException {

    public InvalidPlaylistDataException(String message) {
        super(
            PlaylistErrorCode.INVALID_PLAYLIST_DATA,
            Map.of("reason", message)
        );
    }
}
