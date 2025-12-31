package com.mopl.domain.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public abstract class MoplException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public MoplException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public MoplException(
        ErrorCode errorCode,
        Map<String, Object> details
    ) {
        super(errorCode.getMessage());

        this.errorCode = errorCode;
        this.details = details != null
            ? Collections.unmodifiableMap(details)
            : Collections.emptyMap();
    }
}
