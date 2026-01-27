package com.mopl.kafka.dlq;

import com.mopl.domain.event.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqEventProcessor {

    private static final String HEADER_ORIGINAL_TOPIC = "kafka_dlt-original-topic";
    private static final String HEADER_EXCEPTION_MESSAGE = "kafka_dlt-exception-message";
    private static final String HEADER_EXCEPTION_STACKTRACE = "kafka_dlt-exception-stacktrace";

    private final DlqAlertPublisher dlqAlertPublisher;

    @KafkaListener(
        topics = EventTopic.DLQ,
        groupId = "worker-dlq-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeadLetter(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String originalTopic = extractHeader(record, HEADER_ORIGINAL_TOPIC).orElse("unknown");
            String exceptionMessage = extractHeader(record, HEADER_EXCEPTION_MESSAGE).orElse("unknown");
            String exceptionStackTrace = extractHeader(record, HEADER_EXCEPTION_STACKTRACE).orElse("");

            DlqEvent dlqEvent = DlqEvent.of(
                originalTopic,
                record.key(),
                record.value(),
                exceptionMessage,
                exceptionStackTrace
            );

            dlqAlertPublisher.publish(dlqEvent);

            log.info(
                "[DLQ] Processed dead letter - originalTopic: {}, key: {}, offset: {}",
                originalTopic,
                record.key(),
                record.offset()
            );

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[DLQ] Failed to process dead letter: {}", record.value(), e);
            ack.acknowledge();
        }
    }

    private Optional<String> extractHeader(ConsumerRecord<?, ?> record, String headerKey) {
        Header header = record.headers().lastHeader(headerKey);
        if (header == null || header.value() == null) {
            return Optional.empty();
        }
        return Optional.of(new String(header.value(), StandardCharsets.UTF_8));
    }
}
