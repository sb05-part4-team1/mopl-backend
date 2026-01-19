package com.mopl.websocket.interfaces.api.content;

import com.mopl.api.interfaces.api.watchingsession.WatchingSessionDto;

public record WatchingSessionChange(
    ChangeType type,
    WatchingSessionDto watchingSession,
    long watcherCount
) {
}
