package com.mopl.domain.exception.conversation;

import java.util.Map;
import java.util.UUID;

public class ConversationNotFoundException extends ConversationException {

    public ConversationNotFoundException(UUID id) {
        super(ConversationErrorCode.CONVERSATION_NOT_FOUND, Map.of("id", id));
    }
}
