package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
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
            .receiver(UserModel.builder().id(entity.getReceiver().getId()).build())
            .build();
    }

    public NotificationEntity toEntity(NotificationModel model, UserEntity receiver) {
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
            .receiver(receiver)
            .build();
    }
}
