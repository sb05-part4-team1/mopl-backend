package com.mopl.jpa.entity.base;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter
@SuperBuilder
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
