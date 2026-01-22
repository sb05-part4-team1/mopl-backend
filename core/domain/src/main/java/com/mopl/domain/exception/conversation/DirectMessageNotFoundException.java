package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class DirectMessageNotFoundException extends ConversationException {

    private static final ErrorCode ERROR_CODE = ConversationErrorCode.DIRECT_MESSAGE_NOT_FOUND;

    private DirectMessageNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static DirectMessageNotFoundException withId(UUID id) {
        return new DirectMessageNotFoundException(Map.of("id", id));
    }
}
