package com.mopl.api.interfaces.api.watchingsession;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.user.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {
}
