package com.mopl.domain.repository.notification;

import java.util.Optional;
import java.util.UUID;

import com.mopl.domain.model.notification.NotificationModel;

public interface NotificationRepository {

    Optional<NotificationModel> findByIdAndDeletedAtIsNull(UUID notificationId);

    NotificationModel save(NotificationModel notification);
}
