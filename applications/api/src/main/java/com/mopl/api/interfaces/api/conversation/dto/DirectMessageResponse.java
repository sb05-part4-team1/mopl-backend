package com.mopl.api.interfaces.api.conversation.dto;

import com.mopl.api.interfaces.api.user.dto.UserSummary;
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
