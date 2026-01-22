package com.mopl.domain.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Optional<NotificationModel> findById(UUID notificationId);

    NotificationModel save(NotificationModel notification);

    // 이하 메서드들 cleanup batch 전용
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    int deleteAllByIds(List<UUID> notificationIds);
}
