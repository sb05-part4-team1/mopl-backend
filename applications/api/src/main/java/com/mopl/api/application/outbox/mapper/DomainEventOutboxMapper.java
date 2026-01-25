package com.mopl.api.application.outbox.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.event.DomainEvent;
import com.mopl.domain.exception.outbox.EventSerializationException;
import com.mopl.domain.model.outbox.OutboxModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventOutboxMapper {

    private final ObjectMapper objectMapper;

    public OutboxModel toOutboxModel(DomainEvent event) {
        return OutboxModel.create(
            event.getAggregateType(),
            event.getAggregateId(),
            event.getEventType(),
            event.getTopic(),
            serializePayload(event)
        );
    }

    private String serializePayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw EventSerializationException.withEventTypeAndCause(event.getEventType(), e);
        }
    }
}
