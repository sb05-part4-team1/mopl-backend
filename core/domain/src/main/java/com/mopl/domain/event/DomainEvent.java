package com.mopl.domain.event;

public interface DomainEvent {

    String getAggregateType();

    String getAggregateId();

    String getTopic();

    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
