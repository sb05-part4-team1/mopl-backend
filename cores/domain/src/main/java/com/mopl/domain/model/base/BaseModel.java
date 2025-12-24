package com.mopl.domain.model.base;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseModel {

    private final UUID id;
    private final Instant createdAt;
    private Instant deletedAt;

    protected BaseModel() {
        this.id = null;
        this.createdAt = null;
        this.deletedAt = null;
    }

    protected BaseModel(
        UUID id,
        Instant createdAt,
        Instant deletedAt
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public void delete() {
        if (!isDeleted()) {
            this.deletedAt = Instant.now();
        }
    }

    public void restore() {
        if (isDeleted()) {
            this.deletedAt = null;
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
