package com.mopl.redis.pubsub;

import java.time.Instant;
import java.util.UUID;

public record NotificationMessage(
    UUID notificationId,
    UUID receiverId,
    String title,
    String content,
    String level,
    Instant createdAt
) {

    public static final String CHANNEL = "sse:notifications";
}
