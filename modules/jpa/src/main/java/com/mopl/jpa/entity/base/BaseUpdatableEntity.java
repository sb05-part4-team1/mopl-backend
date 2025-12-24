package com.mopl.jpa.entity.base;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter
public abstract class BaseUpdatableEntity extends BaseEntity {

    @LastModifiedDate
    private Instant updatedAt;

    protected BaseUpdatableEntity() {
    }

    protected BaseUpdatableEntity(
        UUID id,
        Instant createdAt,
        Instant deletedAt,
        Instant updatedAt
    ) {
        super(id, createdAt, deletedAt);
        this.updatedAt = updatedAt;
    }
}
