package com.mopl.jpa.repository.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.jpa.entity.notification.NotificationEntity;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByIdAndDeletedAtIsNull(UUID id);
}
