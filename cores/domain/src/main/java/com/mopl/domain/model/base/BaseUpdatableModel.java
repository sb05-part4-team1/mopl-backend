package com.mopl.domain.model.base;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseUpdatableModel extends BaseModel {

    private Instant updatedAt;

    protected BaseUpdatableModel() {
        super();
    }

    protected BaseUpdatableModel(
        UUID id,
        Instant createdAt,
        Instant deletedAt,
        Instant updatedAt
    ) {
        super(id, createdAt, deletedAt);
        this.updatedAt = updatedAt;
    }
}
