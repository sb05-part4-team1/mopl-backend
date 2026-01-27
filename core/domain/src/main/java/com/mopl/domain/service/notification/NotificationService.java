package com.mopl.domain.service.notification;

import com.mopl.domain.exception.notification.NotificationNotFoundException;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class NotificationService {

    private final NotificationQueryRepository notificationQueryRepository;
    private final NotificationRepository notificationRepository;

    public CursorResponse<NotificationModel> getAll(
        UUID receiverId,
        NotificationQueryRequest request
    ) {
        return notificationQueryRepository.findAll(receiverId, request);
    }

    public NotificationModel getById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));
    }

    public NotificationModel create(NotificationModel notification) {
        return notificationRepository.save(notification);
    }

    public List<NotificationModel> createAll(List<NotificationModel> notifications) {
        return notificationRepository.saveAll(notifications);
    }

    public void deleteById(UUID notificationId) {
        getById(notificationId);
        notificationRepository.delete(notificationId);
    }
}
