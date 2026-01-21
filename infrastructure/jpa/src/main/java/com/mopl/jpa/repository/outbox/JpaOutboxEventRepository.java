package com.mopl.jpa.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.jpa.entity.outbox.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEntity, UUID> {

    @Query("""
        SELECT e FROM OutboxEntity e
        WHERE e.status = :status AND e.retryCount < :maxRetry
        ORDER BY e.createdAt ASC
        LIMIT :limit
        """)
    List<OutboxEntity> findPendingEvents(
        OutboxModel.OutboxStatus status,
        int maxRetry,
        int limit
    );

    @Modifying
    @Query("""
        DELETE FROM OutboxEntity e
        WHERE e.status = :status AND e.publishedAt < :before
        """)
    int deletePublishedEventsBefore(
        OutboxModel.OutboxStatus status,
        Instant before
    );
}
