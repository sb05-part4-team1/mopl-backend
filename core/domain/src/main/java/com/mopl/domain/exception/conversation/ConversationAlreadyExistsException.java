package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ConversationAlreadyExistsException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.CONVERSATION_ALREADY_EXISTS;

    private ConversationAlreadyExistsException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ConversationAlreadyExistsException withParticipants(UUID requesterId, UUID withUserId) {
        return new ConversationAlreadyExistsException(Map.of(
            "requesterId", requesterId,
            "withUserId", withUserId
        ));
    }
}
