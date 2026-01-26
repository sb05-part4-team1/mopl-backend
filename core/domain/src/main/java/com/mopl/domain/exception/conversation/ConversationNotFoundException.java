package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ConversationNotFoundException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.CONVERSATION_NOT_FOUND;

    private ConversationNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ConversationNotFoundException withId(UUID id) {
        return new ConversationNotFoundException(Map.of("id", id));
    }

    public static ConversationNotFoundException withParticipants(UUID requesterId, UUID withUserId) {
        return new ConversationNotFoundException(Map.of(
            "requesterId", requesterId,
            "withUserId", withUserId
        ));
    }
}
