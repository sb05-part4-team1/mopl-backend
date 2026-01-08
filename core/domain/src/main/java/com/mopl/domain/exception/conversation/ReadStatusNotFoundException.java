package com.mopl.domain.exception.conversation;

import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ConversationException {

    public ReadStatusNotFoundException(UUID id) {
        super(ConversationErrorCode.READSTATUS_NOT_FOUND, Map.of("id", id));
    }
}
