package com.mopl.domain.exception.playlist;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaylistForbiddenException extends PlaylistException {

    public PlaylistForbiddenException(
        UUID playlistId,
        UUID requesterId,
        UUID ownerId
    ) {
        super(PlaylistErrorCode.PLAYLIST_FORBIDDEN, buildContext(playlistId, requesterId, ownerId));
    }

    private static Map<String, Object> buildContext(
        UUID playlistId,
        UUID requesterId,
        UUID ownerId
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("playlistId", playlistId);
        context.put("requesterId", requesterId);
        if (ownerId != null) {
            context.put("ownerId", ownerId);
        }
        return context;
    }
}
