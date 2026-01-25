package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.READ_STATUS_NOT_FOUND;

    private ReadStatusNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ReadStatusNotFoundException withId(UUID id) {
        return new ReadStatusNotFoundException(Map.of("id", id));
    }

    public static ReadStatusNotFoundException withParticipantIdAndConversationId(
        UUID participantId,
        UUID conversationId
    ) {
        return new ReadStatusNotFoundException(Map.of(
            "participantId", participantId,
            "conversationId", conversationId
        ));
    }
}
