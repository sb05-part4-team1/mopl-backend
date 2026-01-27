package com.mopl.jpa.repository.notification;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import com.mopl.jpa.support.batch.JdbcBatchInsertHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final TimeBasedEpochGenerator UUID_GENERATOR = Generators.timeBasedEpochGenerator();

    private static final String BATCH_INSERT_SQL = """
        INSERT INTO notifications (id, title, content, level, receiver_id, created_at)
        VALUES (:id, :title, :content, :level, :receiverId, :createdAt)
        """;

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationEntityMapper notificationEntityMapper;
    private final JdbcBatchInsertHelper jdbcBatchInsertHelper;

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
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now();
        List<NotificationModel> notificationsWithId = notifications.stream()
            .map(n -> (NotificationModel) n.toBuilder()
                .id(UUID_GENERATOR.generate())
                .createdAt(now)
                .build())
            .toList();

        jdbcBatchInsertHelper.batchInsert(
            BATCH_INSERT_SQL,
            notificationsWithId,
            this::toParameterSource
        );

        return notificationsWithId;
    }

    private MapSqlParameterSource toParameterSource(NotificationModel notification) {
        return new MapSqlParameterSource()
            .addValue("id", uuidToBytes(notification.getId()))
            .addValue("title", notification.getTitle())
            .addValue("content", notification.getContent())
            .addValue("level", notification.getLevel().name())
            .addValue("receiverId", uuidToBytes(notification.getReceiverId()))
            .addValue("createdAt", notification.getCreatedAt());
    }

    private byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[i + 8] = (byte) (lsb >>> (8 * (7 - i)));
        }
        return bytes;
    }

    @Override
    public void delete(UUID notificationId) {
        jpaNotificationRepository.deleteById(notificationId);
    }
}
