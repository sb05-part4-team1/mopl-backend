package com.mopl.api.interfaces.api.notification;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.notification.NotificationModel;

@Component
public class NotificationResponseMapper {

    public NotificationResponse toResponse(NotificationModel model) {
        if (model == null) {
            return null;
        }

        return new NotificationResponse(
            model.getId(),
            model.getCreatedAt(),
            model.getReceiverId(),
            model.getTitle(),
            model.getContent(),
            model.getLevel()
        );
    }
}
