package com.mopl.domain.exception.notification;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidNotificationDataException extends NotificationException {

    private static final ErrorCode ERROR_CODE = NotificationErrorCode.INVALID_NOTIFICATION_DATA;

    private InvalidNotificationDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidNotificationDataException withDetailMessage(String detailMessage) {
        return new InvalidNotificationDataException(Map.of("detailMessage", detailMessage));
    }
}
