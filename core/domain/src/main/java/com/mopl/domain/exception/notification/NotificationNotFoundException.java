package com.mopl.domain.exception.notification;

import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(UUID id) {
        super(NotificationErrorCode.NOTIFICATION_NOT_FOUND, Map.of("id", id));
    }

}
