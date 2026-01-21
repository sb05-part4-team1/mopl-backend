package com.mopl.jpa.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.jpa.entity.outbox.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaOutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    List<OutboxEntity> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
        OutboxModel.OutboxStatus status,
        int maxRetry,
        Pageable pageable
    );

    int deleteByStatusAndPublishedAtBefore(
        OutboxModel.OutboxStatus status,
        Instant before
    );
}
