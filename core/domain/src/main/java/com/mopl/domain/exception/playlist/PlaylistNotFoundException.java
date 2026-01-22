package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class PlaylistNotFoundException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_NOT_FOUND;

    private PlaylistNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistNotFoundException withId(UUID id) {
        return new PlaylistNotFoundException(Map.of("id", id));
    }
}
