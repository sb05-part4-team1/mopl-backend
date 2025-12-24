package com.mopl.domain.model.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
public abstract class BaseModel {

    private final UUID id;
    private final Instant createdAt;
    private Instant deletedAt;

    protected BaseModel() {
        this.id = null;
        this.createdAt = null;
        this.deletedAt = null;
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
