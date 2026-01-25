package com.mopl.api.interfaces.api.conversation.dto;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.user.UserSummary;

import java.util.UUID;

public record ConversationResponse(
    UUID id,
    UserSummary with,
    DirectMessageResponse lastMessage,
    boolean hasUnread
) {
}
