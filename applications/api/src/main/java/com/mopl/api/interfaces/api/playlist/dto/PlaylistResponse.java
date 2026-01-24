package com.mopl.api.interfaces.api.playlist.dto;

import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
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
