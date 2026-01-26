package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import static com.mopl.domain.model.notification.NotificationModel.LEVEL_MAX_LENGTH;
import static com.mopl.domain.model.notification.NotificationModel.TITLE_MAX_LENGTH;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_receiver_created_at", columnList = "receiver_id, created_at DESC")
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity extends BaseEntity {

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = LEVEL_MAX_LENGTH)
    private NotificationModel.NotificationLevel level;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;
}
