package com.mopl.jpa.entity.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import org.springframework.stereotype.Component;

@Component
public class OutboxEntityMapper {

    public OutboxModel toModel(OutboxEntity entity) {
        if (entity == null) {
            return null;
        }

        return OutboxModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .aggregateType(entity.getAggregateType())
            .aggregateId(entity.getAggregateId())
            .eventType(entity.getEventType())
            .topic(entity.getTopic())
            .payload(entity.getPayload())
            .status(toModelStatus(entity.getStatus()))
            .publishedAt(entity.getPublishedAt())
            .retryCount(entity.getRetryCount())
            .build();
    }

    public OutboxEntity toEntity(OutboxModel model) {
        if (model == null) {
            return null;
        }

        return OutboxEntity.builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .deletedAt(model.getDeletedAt())
            .aggregateType(model.getAggregateType())
            .aggregateId(model.getAggregateId())
            .eventType(model.getEventType())
            .topic(model.getTopic())
            .payload(model.getPayload())
            .status(toEntityStatus(model.getStatus()))
            .publishedAt(model.getPublishedAt())
            .retryCount(model.getRetryCount())
            .build();
    }

    private OutboxModel.OutboxStatus toModelStatus(OutboxEventStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OutboxModel.OutboxStatus.PENDING;
            case PUBLISHED -> OutboxModel.OutboxStatus.PUBLISHED;
            case FAILED -> OutboxModel.OutboxStatus.FAILED;
        };
    }

    private OutboxEventStatus toEntityStatus(OutboxModel.OutboxStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> OutboxEventStatus.PENDING;
            case PUBLISHED -> OutboxEventStatus.PUBLISHED;
            case FAILED -> OutboxEventStatus.FAILED;
        };
    }
}
