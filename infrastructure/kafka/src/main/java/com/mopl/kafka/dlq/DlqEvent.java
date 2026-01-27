package com.mopl.kafka.dlq;

import java.time.Instant;

public record DlqEvent(
    String originalTopic,
    String key,
    String payload,
    String exceptionMessage,
    String exceptionStackTrace,
    Instant occurredAt
) {

    public static DlqEvent of(
        String originalTopic,
        String key,
        String payload,
        String exceptionMessage,
        String exceptionStackTrace
    ) {
        return new DlqEvent(
            originalTopic,
            key,
            payload,
            exceptionMessage,
            exceptionStackTrace,
            Instant.now()
        );
    }
}
