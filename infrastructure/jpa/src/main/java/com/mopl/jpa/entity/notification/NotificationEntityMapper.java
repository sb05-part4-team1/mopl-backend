package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEntityMapper {

    private final UserEntityMapper userEntityMapper;

    public NotificationModel toModel(NotificationEntity notificationEntity) {
        if (notificationEntity == null) {
            return null;
        }

        return buildNotificationModel(
            notificationEntity,
            toReceiverIdOnly(notificationEntity.getReceiver())
        );
    }

    public NotificationModel toModelWithReceiver(NotificationEntity notificationEntity) {
        if (notificationEntity == null) {
            return null;
        }

        return buildNotificationModel(
            notificationEntity,
            userEntityMapper.toModel(notificationEntity.getReceiver())
        );
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
            .receiver(userEntityMapper.toEntity(model.getReceiver()))
            .build();
    }

    private NotificationModel buildNotificationModel(
        NotificationEntity notificationEntity,
        UserModel receiverModel
    ) {
        return NotificationModel.builder()
            .id(notificationEntity.getId())
            .createdAt(notificationEntity.getCreatedAt())
            .deletedAt(notificationEntity.getDeletedAt())
            .title(notificationEntity.getTitle())
            .content(notificationEntity.getContent())
            .level(notificationEntity.getLevel())
            .receiver(receiverModel)
            .build();
    }

    private UserModel toReceiverIdOnly(UserEntity receiverEntity) {
        return receiverEntity != null
            ? UserModel.builder().id(receiverEntity.getId()).build()
            : null;
    }
}
