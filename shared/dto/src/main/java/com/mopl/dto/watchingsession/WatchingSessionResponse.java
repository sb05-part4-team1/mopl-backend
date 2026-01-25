package com.mopl.dto.watchingsession;

import com.mopl.dto.content.ContentSummary;
import com.mopl.dto.user.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionResponse(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {
}
