package com.mopl.domain.exception.outbox;

import java.util.Map;

public class EventSerializationException extends OutboxException {

    public EventSerializationException(String eventType, Throwable cause) {
        super(OutboxErrorCode.EVENT_SERIALIZATION_FAILED,
            Map.of("eventType", eventType, "cause", cause.getMessage()));
    }
}
