package com.mopl.kafka.dlq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DlqEvent 단위 테스트")
class DlqEventTest {

    @Nested
    @DisplayName("of()")
    class OfTest {

        @Test
        @DisplayName("팩토리 메서드로 DlqEvent 생성 시 현재 시간이 설정됨")
        void createsEventWithCurrentTime() {
            // given
            Instant before = Instant.now();

            // when
            DlqEvent event = DlqEvent.of(
                "original-topic",
                "key",
                "payload",
                "exception message",
                "stack trace"
            );

            // then
            Instant after = Instant.now();
            assertThat(event.originalTopic()).isEqualTo("original-topic");
            assertThat(event.key()).isEqualTo("key");
            assertThat(event.payload()).isEqualTo("payload");
            assertThat(event.exceptionMessage()).isEqualTo("exception message");
            assertThat(event.exceptionStackTrace()).isEqualTo("stack trace");
            assertThat(event.occurredAt()).isBetween(before, after);
        }
    }
}
