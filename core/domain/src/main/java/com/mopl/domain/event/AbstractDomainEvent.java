package com.mopl.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;

    protected AbstractDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }
}
