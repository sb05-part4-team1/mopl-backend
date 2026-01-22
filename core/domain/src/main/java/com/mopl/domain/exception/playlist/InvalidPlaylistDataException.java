package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidPlaylistDataException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.INVALID_PLAYLIST_DATA;

    private InvalidPlaylistDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidPlaylistDataException withDetailMessage(String detailMessage) {
        return new InvalidPlaylistDataException(Map.of("detailMessage", detailMessage));
    }
}
