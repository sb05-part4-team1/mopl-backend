package com.mopl.api.application.notification;

import com.mopl.api.interfaces.api.notification.NotificationResponse;
import com.mopl.api.interfaces.api.notification.NotificationResponseMapper;
import com.mopl.domain.exception.notification.NotificationOwnershipException;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;
    private final NotificationResponseMapper notificationResponseMapper;
    private final UserService userService;

    public CursorResponse<NotificationResponse> getNotifications(UUID userId,
        NotificationQueryRequest request) {
        userService.getById(userId);
        return notificationService.getAll(userId, request).map(
            notificationResponseMapper::toResponse);
    }

    public void readNotification(UUID userId, UUID notificationId) {
        userService.getById(userId);
        NotificationModel notification = notificationService.getById(notificationId);

        if (!notification.getReceiverId().equals(userId)) {
            throw new NotificationOwnershipException(notificationId, userId);
        }

        notificationService.deleteById(notificationId);
    }
}
