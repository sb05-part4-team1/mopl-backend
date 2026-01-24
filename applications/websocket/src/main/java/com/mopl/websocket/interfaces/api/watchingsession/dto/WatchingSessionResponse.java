package com.mopl.websocket.interfaces.api.watchingsession.dto;

import com.mopl.websocket.interfaces.api.content.dto.ContentSummary;
import com.mopl.websocket.interfaces.api.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionResponse(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {
}
