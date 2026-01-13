package com.mopl.domain.exception.conversation;

import java.util.Map;
import java.util.UUID;

public class ConversationNotFoundException extends ConversationException {

    public ConversationNotFoundException(UUID id) {
        super(ConversationErrorCode.CONVERSATION_NOT_FOUND, Map.of("id", id));
    }

    public ConversationNotFoundException(UUID userId, UUID withId) {
        super(ConversationErrorCode.CONVERSATION_NOT_FOUND, Map.of("userId",userId,"withId",withId ));
    }
}
