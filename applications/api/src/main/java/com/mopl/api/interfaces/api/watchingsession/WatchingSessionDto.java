package com.mopl.api.interfaces.api.watchingsession;

import java.time.Instant;
import java.util.UUID;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.user.UserSummary;

public record WatchingSessionDto(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {
}
