package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class EventSerializationException extends OutboxException {

    private static final ErrorCode ERROR_CODE = OutboxErrorCode.EVENT_SERIALIZATION_FAILED;

    private EventSerializationException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    public static EventSerializationException withEvnetTypeAndException(
        String eventType,
        Throwable throwable
    ) {
        return new EventSerializationException(ERROR_CODE, Map.of(
            "eventType", eventType,
            "cause", throwable.getMessage())
        );
    }
}
