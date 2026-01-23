package com.mopl.api.interfaces.api.conversation.dto;

import com.mopl.api.interfaces.api.user.dto.UserSummary;
import java.util.UUID;

public record ConversationResponse(
    UUID id,
    UserSummary with,
    DirectMessageResponse lastMessage,
    boolean hasUnread
) {}
