package com.mopl.domain.exception.notification;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

    private static final ErrorCode ERROR_CODE = NotificationErrorCode.NOTIFICATION_NOT_FOUND;

    private NotificationNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static NotificationNotFoundException withId(UUID id) {
        return new NotificationNotFoundException(Map.of("id", id));
    }
}
