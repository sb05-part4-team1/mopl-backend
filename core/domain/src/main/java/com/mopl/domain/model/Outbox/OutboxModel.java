package com.mopl.domain.model.Outbox;

import com.mopl.domain.exception.outbox.InvalidOutboxDataException;
import com.mopl.domain.model.base.BaseModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxModel extends BaseModel {

    public static final int AGGREGATE_TYPE_MAX_LENGTH = 50;
    public static final int AGGREGATE_ID_MAX_LENGTH = 36;
    public static final int EVENT_TYPE_MAX_LENGTH = 100;
    public static final int TOPIC_MAX_LENGTH = 100;

    public enum OutboxStatus {
        PENDING,
        PUBLISHED,
        FAILED
    }

    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String topic;
    private String payload;
    private OutboxStatus status;
    private Instant publishedAt;
    private int retryCount;

    public static OutboxModel create(
        String aggregateType,
        String aggregateId,
        String eventType,
        String topic,
        String payload
    ) {
        validateAggregateType(aggregateType);
        validateAggregateId(aggregateId);
        validateEventType(eventType);
        validateTopic(topic);
        validatePayload(payload);

        return OutboxModel.builder()
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .eventType(eventType)
            .topic(topic)
            .payload(payload)
            .status(OutboxStatus.PENDING)
            .retryCount(0)
            .build();
    }

    public OutboxModel markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        return this;
    }

    public OutboxModel markAsFailed() {
        this.status = OutboxStatus.FAILED;
        return this;
    }

    public OutboxModel incrementRetryCount() {
        this.retryCount++;
        return this;
    }

    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    public boolean isPublished() {
        return this.status == OutboxStatus.PUBLISHED;
    }

    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    private static void validateAggregateType(String aggregateType) {
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new InvalidOutboxDataException("aggregateType은 비어있을 수 없습니다.");
        }
        if (aggregateType.length() > AGGREGATE_TYPE_MAX_LENGTH) {
            throw new InvalidOutboxDataException(
                "aggregateType은 " + AGGREGATE_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateAggregateId(String aggregateId) {
        if (aggregateId == null || aggregateId.isBlank()) {
            throw new InvalidOutboxDataException("aggregateId는 비어있을 수 없습니다.");
        }
        if (aggregateId.length() > AGGREGATE_ID_MAX_LENGTH) {
            throw new InvalidOutboxDataException(
                "aggregateId는 " + AGGREGATE_ID_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new InvalidOutboxDataException("eventType은 비어있을 수 없습니다.");
        }
        if (eventType.length() > EVENT_TYPE_MAX_LENGTH) {
            throw new InvalidOutboxDataException(
                "eventType은 " + EVENT_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new InvalidOutboxDataException("topic은 비어있을 수 없습니다.");
        }
        if (topic.length() > TOPIC_MAX_LENGTH) {
            throw new InvalidOutboxDataException(
                "topic은 " + TOPIC_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validatePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new InvalidOutboxDataException("payload는 비어있을 수 없습니다.");
        }
    }
}
