package com.mopl.api.interfaces.api.watchingsession.dto;

import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {
}
