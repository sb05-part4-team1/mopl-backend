package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class PlaylistContentAlreadyExistsException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_CONTENT_ALREADY_EXISTS;

    private PlaylistContentAlreadyExistsException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistContentAlreadyExistsException withPlaylistIdAndContentId(UUID playlistId, UUID contentId) {
        return new PlaylistContentAlreadyExistsException(Map.of(
            "playlistId", playlistId,
            "contentId", contentId
        ));
    }
}
