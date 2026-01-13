package com.mopl.api.interfaces.api.notification;

import java.time.Instant;
import java.util.UUID;

import com.mopl.domain.model.notification.NotificationLevel;

public record NotificationResponse(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {
}
