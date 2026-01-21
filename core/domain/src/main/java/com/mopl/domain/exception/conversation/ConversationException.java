package com.mopl.domain.exception.conversation;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;
import java.util.Map;

public class ConversationException extends MoplException {

    protected ConversationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
