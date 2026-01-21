package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class OutboxException extends MoplException {

    protected OutboxException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
