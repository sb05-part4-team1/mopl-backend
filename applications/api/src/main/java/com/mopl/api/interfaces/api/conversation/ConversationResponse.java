package com.mopl.api.interfaces.api.conversation;

import com.mopl.api.interfaces.api.user.UserSummary;
import java.util.UUID;

public record ConversationResponse(
    UUID id,
    UserSummary with,
    DirectMessageResponse lastMessage,
    boolean hasUnread
) {
}
