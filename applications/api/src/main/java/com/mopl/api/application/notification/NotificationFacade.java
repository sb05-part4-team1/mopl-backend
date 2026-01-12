package com.mopl.api.application.notification;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.domain.exception.notification.NotificationOwnershipException;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    @Transactional
    public void readNotification(UUID userId, UUID notificationId) {
        NotificationModel notification = notificationService.getById(notificationId);

        if (!notification.getReceiverId().equals(userId)) {
            throw new NotificationOwnershipException(notificationId, userId);
        }

        // 논리 삭제(soft delete)
        notificationService.delete(notificationId);
    }
}
