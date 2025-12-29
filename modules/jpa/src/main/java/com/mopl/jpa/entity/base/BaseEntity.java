package com.mopl.jpa.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity {

    @Id
    @UuidV7
    @Column(columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    private Instant deletedAt;
}
