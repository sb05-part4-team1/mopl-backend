package com.mopl.domain.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Optional<NotificationModel> findById(UUID notificationId);

    NotificationModel save(NotificationModel notification);

    List<NotificationModel> saveAll(List<NotificationModel> notifications);

    void delete(UUID notificationId);
}
