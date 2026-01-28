package com.mopl.kafka.dlq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("LoggingDlqAlertPublisher 단위 테스트")
class LoggingDlqAlertPublisherTest {

    private final LoggingDlqAlertPublisher publisher = new LoggingDlqAlertPublisher();

    @Nested
    @DisplayName("publish()")
    class PublishTest {

        @Test
        @DisplayName("DlqEvent를 로깅으로 발행")
        void withDlqEvent_logsWithoutException() {
            // given
            DlqEvent event = new DlqEvent(
                "original-topic",
                "key",
                "payload",
                "exception message",
                "stack trace",
                Instant.now()
            );

            // when & then
            assertThatCode(() -> publisher.publish(event))
                .doesNotThrowAnyException();
        }
    }
}
