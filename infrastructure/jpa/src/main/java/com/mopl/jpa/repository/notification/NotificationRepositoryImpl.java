package com.mopl.jpa.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationEntityMapper notificationEntityMapper;

    @Override
    public Optional<NotificationModel> findById(UUID notificationId) {
        return jpaNotificationRepository.findById(notificationId)
            .map(notificationEntityMapper::toModel);
    }

    @Override
    public NotificationModel save(NotificationModel notificationModel) {
        NotificationEntity notificationEntity = notificationEntityMapper.toEntity(notificationModel);
        NotificationEntity savedNotificationEntity = jpaNotificationRepository.save(notificationEntity);
        return notificationEntityMapper.toModel(savedNotificationEntity);
    }

    @Override
    public List<NotificationModel> saveAll(List<NotificationModel> notifications) {
        List<NotificationEntity> entities = notifications.stream()
            .map(notificationEntityMapper::toEntity)
            .toList();
        List<NotificationEntity> savedEntities = jpaNotificationRepository.saveAll(entities);
        return savedEntities.stream()
            .map(notificationEntityMapper::toModel)
            .toList();
    }

    @Override
    public void delete(UUID notificationId) {
        jpaNotificationRepository.deleteById(notificationId);
    }
}
