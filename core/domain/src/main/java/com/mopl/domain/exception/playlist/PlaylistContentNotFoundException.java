package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class PlaylistContentNotFoundException extends PlaylistException {

    private static final ErrorCode ERROR_CODE = PlaylistErrorCode.PLAYLIST_CONTENT_NOT_FOUND;

    private PlaylistContentNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static PlaylistContentNotFoundException withPlaylistIdAndContentId(UUID playlistId, UUID contentId) {
        return new PlaylistContentNotFoundException(Map.of("playlistId", playlistId, "contentId", contentId));
    }
}
