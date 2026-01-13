package com.mopl.jpa.repository.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.user.JpaUserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationEntityMapper notificationEntityMapper;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<NotificationModel> findByIdAndDeletedAtIsNull(UUID notificationId) {
        return jpaNotificationRepository.findByIdAndDeletedAtIsNull(notificationId)
            .map(notificationEntityMapper::toModel);
    }

    @Override
    public NotificationModel save(NotificationModel notification) {
        UserEntity receiver = jpaUserRepository.getReferenceById(notification.getReceiverId());

        NotificationEntity entity = notificationEntityMapper.toEntity(notification, receiver);
        NotificationEntity saved = jpaNotificationRepository.save(entity);

        return notificationEntityMapper.toModel(saved);
    }
}
