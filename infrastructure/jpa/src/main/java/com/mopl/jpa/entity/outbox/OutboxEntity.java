package com.mopl.jpa.entity.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

import static com.mopl.domain.model.outbox.OutboxModel.AGGREGATE_ID_MAX_LENGTH;
import static com.mopl.domain.model.outbox.OutboxModel.AGGREGATE_TYPE_MAX_LENGTH;
import static com.mopl.domain.model.outbox.OutboxModel.EVENT_TYPE_MAX_LENGTH;
import static com.mopl.domain.model.outbox.OutboxModel.STATUS_MAX_LENGTH;
import static com.mopl.domain.model.outbox.OutboxModel.TOPIC_MAX_LENGTH;

@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEntity extends BaseEntity {

    @Column(nullable = false, length = AGGREGATE_TYPE_MAX_LENGTH)
    private String aggregateType;

    @Column(nullable = false, length = AGGREGATE_ID_MAX_LENGTH)
    private String aggregateId;

    @Column(nullable = false, length = EVENT_TYPE_MAX_LENGTH)
    private String eventType;

    @Column(nullable = false, length = TOPIC_MAX_LENGTH)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = STATUS_MAX_LENGTH)
    private OutboxModel.OutboxStatus status;

    private Instant publishedAt;

    @Column(nullable = false)
    private int retryCount;
}
