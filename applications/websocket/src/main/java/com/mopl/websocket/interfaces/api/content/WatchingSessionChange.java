package com.mopl.websocket.interfaces.api.content;

import com.mopl.dto.watchingsession.WatchingSessionResponse;

public record WatchingSessionChange(
    ChangeType type,
    WatchingSessionResponse watchingSession,
    long watcherCount
) {
}
