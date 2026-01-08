package com.mopl.api.interfaces.api.playlist;

import com.mopl.api.interfaces.api.content.ContentSummary;
import com.mopl.api.interfaces.api.user.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
    UUID id,
    UserSummary owner,
    String title,
    String description,
    Instant updatedAt,
    long subscriberCount,
    boolean subscribedByMe,
    List<ContentSummary> contents
) {
}
