package com.mopl.dto.conversation;

import com.mopl.dto.user.UserSummary;

import java.util.UUID;

public record ConversationResponse(
    UUID id,
    UserSummary with,
    DirectMessageResponse lastMessage,
    boolean hasUnread
) {
}
