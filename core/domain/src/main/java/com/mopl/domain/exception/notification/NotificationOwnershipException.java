package com.mopl.domain.exception.notification;

import java.util.Map;
import java.util.UUID;

public class NotificationOwnershipException extends NotificationException {

    public NotificationOwnershipException(UUID notificationId, UUID userId) {
        super(NotificationErrorCode.NOTIFICATION_OWNERSHIP_MISMATCH,
            Map.of("notificationId", notificationId, "userId", userId));
    }
}
