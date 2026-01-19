package com.mopl.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID getEventId();

    Instant getOccurredAt();

    String getAggregateType();

    String getAggregateId();

    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
