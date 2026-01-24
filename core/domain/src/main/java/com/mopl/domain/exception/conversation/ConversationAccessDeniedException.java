package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ConversationAccessDeniedException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.CONVERSATION_ACCESS_DENIED;

    private ConversationAccessDeniedException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static ConversationAccessDeniedException withUserIdAndConversationId(
        UUID userId,
        UUID conversationId
    ) {
        return new ConversationAccessDeniedException(
            Map.of(
                "userId", userId,
                "conversationId", conversationId
            )
        );
    }
}
