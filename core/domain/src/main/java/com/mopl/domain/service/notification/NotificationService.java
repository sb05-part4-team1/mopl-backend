package com.mopl.domain.service.notification;

import java.util.UUID;

import com.mopl.domain.exception.notification.NotificationNotFoundException;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void deleteById(UUID notificationId) {
        NotificationModel notification = getById(notificationId);
        notification.delete();
        notificationRepository.save(notification);
    }

    public NotificationModel getById(UUID notificationId) {
        return notificationRepository.findByIdAndDeletedAtIsNull(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }
}
