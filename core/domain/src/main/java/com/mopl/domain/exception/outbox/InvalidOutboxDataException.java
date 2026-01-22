package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidOutboxDataException extends OutboxException {

    private static final ErrorCode ERROR_CODE = OutboxErrorCode.INVALID_OUTBOX_DATA;

    private InvalidOutboxDataException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static InvalidOutboxDataException withDetailMessage(String detailMessage) {
        return new InvalidOutboxDataException(Map.of("detailMessage", detailMessage));
    }
}
