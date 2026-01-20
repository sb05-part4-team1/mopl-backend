package com.mopl.api.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.event.DomainEvent;
import com.mopl.jpa.entity.outbox.OutboxEventEntity;
import com.mopl.jpa.entity.outbox.OutboxEventStatus;
import com.mopl.jpa.repository.outbox.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final JpaOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(DomainEvent event) {
        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
            .aggregateType(event.getAggregateType())
            .aggregateId(event.getAggregateId())
            .eventType(event.getEventType())
            .topic(event.getTopic())
            .payload(serializePayload(event))
            .status(OutboxEventStatus.PENDING)
            .retryCount(0)
            .build();

        outboxEventRepository.save(outboxEvent);
    }

    private String serializePayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                "Failed to serialize event: "
                    + event.getEventType(), e);
        }
    }
}
