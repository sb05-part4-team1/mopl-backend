package com.mopl.websocket.interfaces.api.conversation.dto;

import com.mopl.websocket.interfaces.api.user.dto.UserSummary;

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
