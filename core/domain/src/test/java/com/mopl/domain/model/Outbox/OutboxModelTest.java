package com.mopl.domain.model.Outbox;

import com.mopl.domain.exception.outbox.InvalidOutboxDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static com.mopl.domain.model.Outbox.OutboxModel.AGGREGATE_ID_MAX_LENGTH;
import static com.mopl.domain.model.Outbox.OutboxModel.AGGREGATE_TYPE_MAX_LENGTH;
import static com.mopl.domain.model.Outbox.OutboxModel.EVENT_TYPE_MAX_LENGTH;
import static com.mopl.domain.model.Outbox.OutboxModel.TOPIC_MAX_LENGTH;
import static com.mopl.domain.model.Outbox.OutboxModel.OutboxStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OutboxModel 단위 테스트")
class OutboxModelTest {

    private static final String VALID_AGGREGATE_TYPE = "Notification";
    private static final String VALID_AGGREGATE_ID = UUID.randomUUID().toString();
    private static final String VALID_EVENT_TYPE = "NotificationCreated";
    private static final String VALID_TOPIC = "notification-events";
    private static final String VALID_PAYLOAD = "{\"id\":\"123\",\"message\":\"test\"}";

    @Nested
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void withBuilder_initializesAllFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant publishedAt = Instant.now();

            // when
            OutboxModel outbox = OutboxModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(null)
                .aggregateType(VALID_AGGREGATE_TYPE)
                .aggregateId(VALID_AGGREGATE_ID)
                .eventType(VALID_EVENT_TYPE)
                .topic(VALID_TOPIC)
                .payload(VALID_PAYLOAD)
                .status(OutboxStatus.PUBLISHED)
                .publishedAt(publishedAt)
                .retryCount(3)
                .build();

            // then
            assertThat(outbox.getId()).isEqualTo(id);
            assertThat(outbox.getCreatedAt()).isEqualTo(createdAt);
            assertThat(outbox.getDeletedAt()).isNull();
            assertThat(outbox.getAggregateType()).isEqualTo(VALID_AGGREGATE_TYPE);
            assertThat(outbox.getAggregateId()).isEqualTo(VALID_AGGREGATE_ID);
            assertThat(outbox.getEventType()).isEqualTo(VALID_EVENT_TYPE);
            assertThat(outbox.getTopic()).isEqualTo(VALID_TOPIC);
            assertThat(outbox.getPayload()).isEqualTo(VALID_PAYLOAD);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            assertThat(outbox.getPublishedAt()).isEqualTo(publishedAt);
            assertThat(outbox.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 OutboxModel 생성")
        void withValidData_createsOutboxModel() {
            // when
            OutboxModel outbox = OutboxModel.create(
                VALID_AGGREGATE_TYPE,
                VALID_AGGREGATE_ID,
                VALID_EVENT_TYPE,
                VALID_TOPIC,
                VALID_PAYLOAD
            );

            // then
            assertThat(outbox.getAggregateType()).isEqualTo(VALID_AGGREGATE_TYPE);
            assertThat(outbox.getAggregateId()).isEqualTo(VALID_AGGREGATE_ID);
            assertThat(outbox.getEventType()).isEqualTo(VALID_EVENT_TYPE);
            assertThat(outbox.getTopic()).isEqualTo(VALID_TOPIC);
            assertThat(outbox.getPayload()).isEqualTo(VALID_PAYLOAD);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isZero();
            assertThat(outbox.getPublishedAt()).isNull();
        }

        static Stream<Arguments> invalidAggregateTypeProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidAggregateTypeProvider")
        @DisplayName("aggregateType이 비어있으면 예외 발생")
        void withEmptyAggregateType_throwsException(String description, String aggregateType) {
            assertThatThrownBy(() -> OutboxModel.create(
                aggregateType, VALID_AGGREGATE_ID, VALID_EVENT_TYPE, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("aggregateType은 비어있을 수 없습니다."));
        }

        @Test
        @DisplayName("aggregateType이 50자 초과하면 예외 발생")
        void withAggregateTypeExceedingMaxLength_throwsException() {
            String longAggregateType = "a".repeat(AGGREGATE_TYPE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> OutboxModel.create(
                longAggregateType, VALID_AGGREGATE_ID, VALID_EVENT_TYPE, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("aggregateType은 " + AGGREGATE_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        static Stream<Arguments> invalidAggregateIdProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidAggregateIdProvider")
        @DisplayName("aggregateId가 비어있으면 예외 발생")
        void withEmptyAggregateId_throwsException(String description, String aggregateId) {
            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, aggregateId, VALID_EVENT_TYPE, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("aggregateId는 비어있을 수 없습니다."));
        }

        @Test
        @DisplayName("aggregateId가 36자 초과하면 예외 발생")
        void withAggregateIdExceedingMaxLength_throwsException() {
            String longAggregateId = "a".repeat(AGGREGATE_ID_MAX_LENGTH + 1);

            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, longAggregateId, VALID_EVENT_TYPE, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("aggregateId는 " + AGGREGATE_ID_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        static Stream<Arguments> invalidEventTypeProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidEventTypeProvider")
        @DisplayName("eventType이 비어있으면 예외 발생")
        void withEmptyEventType_throwsException(String description, String eventType) {
            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, VALID_AGGREGATE_ID, eventType, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("eventType은 비어있을 수 없습니다."));
        }

        @Test
        @DisplayName("eventType이 100자 초과하면 예외 발생")
        void withEventTypeExceedingMaxLength_throwsException() {
            String longEventType = "a".repeat(EVENT_TYPE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, VALID_AGGREGATE_ID, longEventType, VALID_TOPIC, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("eventType은 " + EVENT_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        static Stream<Arguments> invalidTopicProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTopicProvider")
        @DisplayName("topic이 비어있으면 예외 발생")
        void withEmptyTopic_throwsException(String description, String topic) {
            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, VALID_AGGREGATE_ID, VALID_EVENT_TYPE, topic, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("topic은 비어있을 수 없습니다."));
        }

        @Test
        @DisplayName("topic이 100자 초과하면 예외 발생")
        void withTopicExceedingMaxLength_throwsException() {
            String longTopic = "a".repeat(TOPIC_MAX_LENGTH + 1);

            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, VALID_AGGREGATE_ID, VALID_EVENT_TYPE, longTopic, VALID_PAYLOAD
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("topic은 " + TOPIC_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        static Stream<Arguments> invalidPayloadProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPayloadProvider")
        @DisplayName("payload가 비어있으면 예외 발생")
        void withEmptyPayload_throwsException(String description, String payload) {
            assertThatThrownBy(() -> OutboxModel.create(
                VALID_AGGREGATE_TYPE, VALID_AGGREGATE_ID, VALID_EVENT_TYPE, VALID_TOPIC, payload
            ))
                .isInstanceOf(InvalidOutboxDataException.class)
                .satisfies(e -> assertThat(((InvalidOutboxDataException) e).getDetails().get("detailMessage"))
                    .isEqualTo("payload는 비어있을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("markAsPublished()")
    class MarkAsPublishedTest {

        @Test
        @DisplayName("상태가 PUBLISHED로 변경되고 publishedAt이 설정됨")
        void withPendingOutbox_changesStatusToPublished() {
            // given
            OutboxModel outbox = createValidOutbox();
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.getPublishedAt()).isNull();

            // when
            OutboxModel result = outbox.markAsPublished();

            // then
            assertThat(result).isSameAs(outbox);
            assertThat(outbox.isPublished()).isTrue();
            assertThat(outbox.getPublishedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("markAsFailed()")
    class MarkAsFailedTest {

        @Test
        @DisplayName("상태가 FAILED로 변경됨")
        void withPendingOutbox_changesStatusToFailed() {
            // given
            OutboxModel outbox = createValidOutbox();
            assertThat(outbox.isPending()).isTrue();

            // when
            OutboxModel result = outbox.markAsFailed();

            // then
            assertThat(result).isSameAs(outbox);
            assertThat(outbox.isFailed()).isTrue();
        }
    }

    @Nested
    @DisplayName("incrementRetryCount()")
    class IncrementRetryCountTest {

        @Test
        @DisplayName("retryCount가 1씩 증가함")
        void withOutbox_incrementsRetryCount() {
            // given
            OutboxModel outbox = createValidOutbox();
            assertThat(outbox.getRetryCount()).isZero();

            // when
            OutboxModel result = outbox.incrementRetryCount();
            outbox.incrementRetryCount();
            outbox.incrementRetryCount();

            // then
            assertThat(result).isSameAs(outbox);
            assertThat(outbox.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드")
    class StatusCheckTest {

        @Test
        @DisplayName("각 상태에 따라 올바른 boolean 반환")
        void withDifferentStatuses_returnsCorrectBoolean() {
            // PENDING
            OutboxModel pending = createValidOutbox();
            assertThat(pending.isPending()).isTrue();
            assertThat(pending.isPublished()).isFalse();
            assertThat(pending.isFailed()).isFalse();

            // PUBLISHED
            OutboxModel published = createValidOutbox();
            published.markAsPublished();
            assertThat(published.isPending()).isFalse();
            assertThat(published.isPublished()).isTrue();
            assertThat(published.isFailed()).isFalse();

            // FAILED
            OutboxModel failed = createValidOutbox();
            failed.markAsFailed();
            assertThat(failed.isPending()).isFalse();
            assertThat(failed.isPublished()).isFalse();
            assertThat(failed.isFailed()).isTrue();
        }
    }

    private OutboxModel createValidOutbox() {
        return OutboxModel.create(
            VALID_AGGREGATE_TYPE,
            VALID_AGGREGATE_ID,
            VALID_EVENT_TYPE,
            VALID_TOPIC,
            VALID_PAYLOAD
        );
    }
}
