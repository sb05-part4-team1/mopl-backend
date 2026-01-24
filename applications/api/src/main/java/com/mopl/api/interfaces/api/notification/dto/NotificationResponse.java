package com.mopl.api.interfaces.api.notification.dto;

import com.mopl.domain.model.notification.NotificationModel;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationModel.NotificationLevel level
) {
}
