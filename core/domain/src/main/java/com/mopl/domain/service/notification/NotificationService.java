package com.mopl.domain.service.notification;

import com.mopl.domain.exception.notification.NotificationNotFoundException;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationQueryRepository notificationQueryRepository;

    public NotificationModel create(NotificationModel notification) {
        return notificationRepository.save(notification);
    }

    public void deleteById(UUID notificationId) {
        NotificationModel notification = getById(notificationId);
        notification.delete();
        notificationRepository.save(notification);
    }

    public NotificationModel getById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    }

    public CursorResponse<NotificationModel> getAll(
        UUID receiverId,
        NotificationQueryRequest request
    ) {
        return notificationQueryRepository.findAll(receiverId, request);
    }
}
