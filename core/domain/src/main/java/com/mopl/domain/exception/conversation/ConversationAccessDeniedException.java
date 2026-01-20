package com.mopl.domain.exception.conversation;

import java.util.Map;
import java.util.UUID;

public class ConversationAccessDeniedException extends ConversationException {

    public ConversationAccessDeniedException(UUID conversationId, UUID userId) {
        super(ConversationErrorCode.CONVERSATION_NOT_PARTICIPANT,
            Map.of("conversationId", conversationId, "userId", userId));
    }
}
