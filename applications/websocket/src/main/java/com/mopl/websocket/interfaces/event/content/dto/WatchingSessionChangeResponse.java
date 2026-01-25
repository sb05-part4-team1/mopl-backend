package com.mopl.websocket.interfaces.event.content.dto;

import com.mopl.dto.watchingsession.WatchingSessionResponse;

public record WatchingSessionChangeResponse(
    WatchingSessionChangeType type,
    WatchingSessionResponse watchingSession,
    long watcherCount
) {
}
