package com.mopl.api.interfaces.api.notification;

import com.mopl.domain.model.notification.NotificationModel;
import org.springframework.stereotype.Component;

@Component
public class NotificationResponseMapper {

    public NotificationResponse toResponse(NotificationModel model) {
        if (model == null) {
            return null;
        }

        return new NotificationResponse(
            model.getId(),
            model.getCreatedAt(),
            model.getReceiver().getId(),
            model.getTitle(),
            model.getContent(),
            model.getLevel()
        );
    }
}
