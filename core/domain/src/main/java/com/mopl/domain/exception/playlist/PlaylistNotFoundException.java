package com.mopl.domain.exception.playlist;

import java.util.Map;
import java.util.UUID;

public class PlaylistNotFoundException extends PlaylistException {

    public PlaylistNotFoundException(UUID id) {
        super(PlaylistErrorCode.PLAYLIST_NOT_FOUND, Map.of("id", id));
    }
}
