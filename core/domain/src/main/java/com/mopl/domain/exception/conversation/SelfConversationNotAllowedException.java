package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class SelfConversationNotAllowedException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.SELF_CONVERSATION_NOT_ALLOWED;

    private SelfConversationNotAllowedException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static SelfConversationNotAllowedException withUserId(UUID userId) {
        return new SelfConversationNotAllowedException(Map.of("userId", userId));
    }
}
