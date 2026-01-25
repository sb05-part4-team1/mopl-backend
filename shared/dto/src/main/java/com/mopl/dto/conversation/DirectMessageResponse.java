package com.mopl.dto.conversation;

import com.mopl.dto.user.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageResponse(
    UUID id,
    UUID conversationId,
    Instant createdAt,
    UserSummary sender,
    UserSummary receiver,
    String content
) {
}
