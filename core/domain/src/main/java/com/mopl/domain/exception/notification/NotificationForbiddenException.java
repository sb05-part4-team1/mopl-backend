package com.mopl.domain.exception.notification;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class NotificationForbiddenException extends NotificationException {

    private static final ErrorCode ERROR_CODE = NotificationErrorCode.NOTIFICATION_FORBIDDEN;

    private NotificationForbiddenException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static NotificationForbiddenException withNotificationIdAndUserId(UUID notificationId, UUID userId) {
        return new NotificationForbiddenException(Map.of("notificationId", notificationId, "userId", userId));
    }
}
