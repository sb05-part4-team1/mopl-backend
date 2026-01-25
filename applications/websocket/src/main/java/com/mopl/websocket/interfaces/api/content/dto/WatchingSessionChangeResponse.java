package com.mopl.websocket.interfaces.api.content.dto;

import com.mopl.dto.watchingsession.WatchingSessionResponse;

public record WatchingSessionChangeResponse(
    WatchingSessionChangeType type,
    WatchingSessionResponse watchingSession,
    long watcherCount
) {
}
