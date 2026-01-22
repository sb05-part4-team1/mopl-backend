package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class EventSerializationException extends OutboxException {

    private static final ErrorCode ERROR_CODE = OutboxErrorCode.EVENT_SERIALIZATION_FAILED;

    private EventSerializationException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static EventSerializationException withEventTypeAndCause(String eventType, Throwable cause) {
        return new EventSerializationException(Map.of("eventType", eventType, "cause", cause.getMessage()));
    }
}
