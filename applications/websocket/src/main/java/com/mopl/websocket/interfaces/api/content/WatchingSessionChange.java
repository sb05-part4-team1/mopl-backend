package com.mopl.websocket.interfaces.api.content;

import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;

public record WatchingSessionChange(
    ChangeType type,
    WatchingSessionResponse watchingSession,
    long watcherCount
) {
}
