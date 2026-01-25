package com.mopl.jpa.repository.notification;

import com.mopl.jpa.entity.notification.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    @Query(
        value = """
                select BIN_TO_UUID(id)
                from notifications
                where deleted_at is not null
                  and deleted_at < :threshold
                order by deleted_at
                limit :limit
            """,
        nativeQuery = true
    )
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
                delete from notifications
                where id in (:notificationIds)
            """,
        nativeQuery = true
    )
    int deleteByIdIn(List<UUID> notificationIds);
}
