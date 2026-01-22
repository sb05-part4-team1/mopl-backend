package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaylistForbiddenException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_FORBIDDEN;

    private PlaylistForbiddenException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistForbiddenException withPlaylistIdAndRequesterIdAndOwnerId(
        UUID playlistId,
        UUID requesterId,
        UUID ownerId
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("playlistId", playlistId);
        details.put("requesterId", requesterId);
        if (ownerId != null) {
            details.put("ownerId", ownerId);
        }
        return new PlaylistForbiddenException(details);
    }
}
