package com.mopl.domain.exception.notification;

import java.util.Map;

public class InvalidNotificationDataException extends NotificationException {

    public InvalidNotificationDataException(String detailMessage) {
        super(NotificationErrorCode.INVALID_NOTIFICATION_DATA, Map.of("detailMessage", detailMessage));
    }
}
