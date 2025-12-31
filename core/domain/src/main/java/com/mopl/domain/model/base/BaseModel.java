package com.mopl.domain.model.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseModel {

    private UUID id;
    private Instant createdAt;
    private Instant deletedAt;

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
