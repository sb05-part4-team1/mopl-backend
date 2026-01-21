package com.mopl.domain.exception.outbox;

import java.util.Map;

public class InvalidOutboxDataException extends OutboxException {

    public InvalidOutboxDataException(String detailMessage) {
        super(OutboxErrorCode.INVALID_OUTBOX_DATA, Map.of("detailMessage", detailMessage));
    }
}
