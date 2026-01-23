package com.mopl.jpa.repository.notification;

import com.mopl.jpa.entity.notification.NotificationEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    // 이하 메서드들 cleanup batch 전용
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
    List<UUID> findCleanupTargets(
        @Param("threshold") Instant threshold,
        @Param("limit") int limit
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
                delete from notifications
                where id in (:notificationIds)
            """,
        nativeQuery = true
    )
    int deleteAllByIds(@Param("notificationIds") List<UUID> notificationIds);
}
