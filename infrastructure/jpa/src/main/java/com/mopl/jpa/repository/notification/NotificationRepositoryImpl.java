package com.mopl.jpa.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.user.JpaUserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationEntityMapper notificationEntityMapper;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<NotificationModel> findById(UUID notificationId) {
        return jpaNotificationRepository.findById(notificationId)
            .map(notificationEntityMapper::toModel);
    }

    @Override
    public NotificationModel save(NotificationModel notification) {
        UserEntity receiver = jpaUserRepository.getReferenceById(notification.getReceiver()
            .getId());

        NotificationEntity entity = notificationEntityMapper.toEntity(notification, receiver);
        NotificationEntity saved = jpaNotificationRepository.save(entity);

        return notificationEntityMapper.toModel(saved);
    }

    // 이하 메서드들 cleanup batch 전용
    @Override
    public List<UUID> findCleanupTargets(Instant threshold, int limit) {
        return jpaNotificationRepository.findCleanupTargets(threshold, limit);
    }

    @Override
    public int deleteAllByIds(List<UUID> notificationIds) {
        return jpaNotificationRepository.deleteAllByIds(notificationIds);
    }
}
