package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public NotificationModel toModel(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return NotificationModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .title(entity.getTitle())
            .content(entity.getContent())
            .level(entity.getLevel())
            .receiverId(entity.getReceiverId())
            .build();
    }

    public NotificationEntity toEntity(NotificationModel model) {
        if (model == null) {
            return null;
        }

        return NotificationEntity.builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .deletedAt(model.getDeletedAt())
            .title(model.getTitle())
            .content(model.getContent())
            .level(model.getLevel())
            .receiverId(model.getReceiverId())
            .build();
    }
}
