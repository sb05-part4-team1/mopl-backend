package com.mopl.domain.exception.conversation;

import java.util.Map;
import java.util.UUID;

public class DirectMessageNotFoundException extends ConversationException {

    public DirectMessageNotFoundException(UUID id) {
        super(ConversationErrorCode.DIRECTMESSAGE_NOT_FOUND, Map.of("id", id));
    }

}
