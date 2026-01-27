package com.mopl.kafka.dlq;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingDlqAlertPublisher implements DlqAlertPublisher {

    @Override
    public void publish(DlqEvent event) {
        log.error(
            "[DLQ Alert] Failed message - topic: {}, key: {}, payload: {}, exception: {}",
            event.originalTopic(),
            event.key(),
            event.payload(),
            event.exceptionMessage()
        );
    }
}
