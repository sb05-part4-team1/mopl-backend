package com.mopl.sse.application;

import com.mopl.domain.model.notification.NotificationModel;

import java.time.Instant;
import java.util.UUID;

public record SseNotificationData(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationModel.NotificationLevel level
) {

    public static SseNotificationData from(NotificationModel model) {
        return new SseNotificationData(
            model.getId(),
            model.getCreatedAt(),
            model.getReceiver().getId(),
            model.getTitle(),
            model.getContent(),
            model.getLevel()
        );
    }
}
