package com.mopl.jpa.repository.outbox;

import com.mopl.jpa.entity.outbox.OutboxEventEntity;
import com.mopl.jpa.entity.outbox.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("""
        SELECT e FROM OutboxEventEntity e
        WHERE e.status = :status AND e.retryCount < :maxRetry
        ORDER BY e.createdAt ASC
        LIMIT :limit
        """)
    List<OutboxEventEntity> findPendingEvents(
        @Param("status") OutboxEventStatus status,
        @Param("maxRetry") int maxRetry,
        @Param("limit") int limit
    );

    @Modifying
    @Query("""
        DELETE FROM OutboxEventEntity e
        WHERE e.status = :status AND e.publishedAt < :before
        """)
    int deletePublishedEventsBefore(
        @Param("status") OutboxEventStatus status,
        @Param("before") Instant before
    );
}
