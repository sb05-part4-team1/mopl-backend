package com.mopl.kafka.dlq;

public interface DlqAlertPublisher {

    void publish(DlqEvent event);
}
