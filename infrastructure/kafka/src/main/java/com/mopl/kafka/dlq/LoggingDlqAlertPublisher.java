package com.mopl.kafka.dlq;

import com.mopl.logging.context.LogContext;

public class LoggingDlqAlertPublisher implements DlqAlertPublisher {

    @Override
    public void publish(DlqEvent event) {
        LogContext.with("topic", event.originalTopic())
            .and("key", event.key())
            .and("payload", event.payload())
            .and("exception", event.exceptionMessage())
            .error("[DLQ Alert] Failed message");
    }
}
