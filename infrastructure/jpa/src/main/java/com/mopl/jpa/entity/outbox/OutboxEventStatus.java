package com.mopl.jpa.entity.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
