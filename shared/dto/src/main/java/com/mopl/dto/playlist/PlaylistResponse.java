package com.mopl.dto.playlist;

import com.mopl.dto.content.ContentSummary;
import com.mopl.dto.user.UserSummary;

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
