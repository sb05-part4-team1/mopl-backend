package com.mopl.kafka.dlq;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DlqEventProcessor 단위 테스트")
class DlqEventProcessorTest {

    @Mock
    private DlqAlertPublisher dlqAlertPublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private DlqEventProcessor processor;

    @Nested
    @DisplayName("handleDeadLetter()")
    class HandleDeadLetterTest {

        @Test
        @DisplayName("헤더가 있는 DLQ 메시지를 처리하고 alert를 발행")
        void withHeaders_publishesAlertAndAcknowledges() {
            // given
            RecordHeaders headers = new RecordHeaders();
            headers.add("kafka_dlt-original-topic", "original-topic".getBytes(StandardCharsets.UTF_8));
            headers.add("kafka_dlt-exception-message", "Test exception".getBytes(StandardCharsets.UTF_8));
            headers.add("kafka_dlt-exception-stacktrace", "stack trace".getBytes(StandardCharsets.UTF_8));

            ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "dlq-topic", 0, 100L, "test-key", "test-payload"
            );
            record.headers().add("kafka_dlt-original-topic", "original-topic".getBytes(StandardCharsets.UTF_8));
            record.headers().add("kafka_dlt-exception-message", "Test exception".getBytes(StandardCharsets.UTF_8));
            record.headers().add("kafka_dlt-exception-stacktrace", "stack trace".getBytes(StandardCharsets.UTF_8));

            // when
            processor.handleDeadLetter(record, acknowledgment);

            // then
            ArgumentCaptor<DlqEvent> captor = ArgumentCaptor.forClass(DlqEvent.class);
            then(dlqAlertPublisher).should().publish(captor.capture());

            DlqEvent event = captor.getValue();
            assertThat(event.originalTopic()).isEqualTo("original-topic");
            assertThat(event.key()).isEqualTo("test-key");
            assertThat(event.payload()).isEqualTo("test-payload");
            assertThat(event.exceptionMessage()).isEqualTo("Test exception");
            assertThat(event.exceptionStackTrace()).isEqualTo("stack trace");

            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("헤더가 없는 DLQ 메시지는 unknown으로 처리")
        void withoutHeaders_usesUnknownValues() {
            // given
            ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "dlq-topic", 0, 100L, "test-key", "test-payload"
            );

            // when
            processor.handleDeadLetter(record, acknowledgment);

            // then
            ArgumentCaptor<DlqEvent> captor = ArgumentCaptor.forClass(DlqEvent.class);
            then(dlqAlertPublisher).should().publish(captor.capture());

            DlqEvent event = captor.getValue();
            assertThat(event.originalTopic()).isEqualTo("unknown");
            assertThat(event.exceptionMessage()).isEqualTo("unknown");
            assertThat(event.exceptionStackTrace()).isEmpty();

            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("publisher에서 예외 발생 시에도 acknowledge 호출")
        void whenPublisherThrows_stillAcknowledges() {
            // given
            ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "dlq-topic", 0, 100L, "test-key", "test-payload"
            );
            org.mockito.BDDMockito.willThrow(new RuntimeException("publish failed"))
                .given(dlqAlertPublisher).publish(org.mockito.ArgumentMatchers.any());

            // when
            processor.handleDeadLetter(record, acknowledgment);

            // then
            then(acknowledgment).should().acknowledge();
        }

        @Test
        @DisplayName("헤더 값이 null이면 unknown으로 처리")
        void withNullHeaderValue_usesUnknownValues() {
            // given
            ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "dlq-topic", 0, 100L, "test-key", "test-payload"
            );
            record.headers().add("kafka_dlt-original-topic", null);
            record.headers().add("kafka_dlt-exception-message", null);

            // when
            processor.handleDeadLetter(record, acknowledgment);

            // then
            ArgumentCaptor<DlqEvent> captor = ArgumentCaptor.forClass(DlqEvent.class);
            then(dlqAlertPublisher).should().publish(captor.capture());

            DlqEvent event = captor.getValue();
            assertThat(event.originalTopic()).isEqualTo("unknown");
            assertThat(event.exceptionMessage()).isEqualTo("unknown");

            then(acknowledgment).should().acknowledge();
        }
    }
}
