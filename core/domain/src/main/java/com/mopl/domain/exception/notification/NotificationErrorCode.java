package com.mopl.domain.exception.notification;

import com.mopl.domain.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다."),
    NOTIFICATION_OWNERSHIP_MISMATCH(403, "알림에 대한 권한이 없습니다.");

    private final int status;
    private final String message;
}
