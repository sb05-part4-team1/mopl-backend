package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class InvalidOutboxDataException extends OutboxException {

    private static final ErrorCode ERROR_CODE = OutboxErrorCode.INVALID_OUTBOX_DATA;

    public InvalidOutboxDataException(String detailMessage) {
        super(ERROR_CODE, Map.of("detailMessage", detailMessage));
    }
}
