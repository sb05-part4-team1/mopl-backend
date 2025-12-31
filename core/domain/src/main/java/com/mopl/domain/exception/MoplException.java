package com.mopl.domain.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public abstract class MoplException extends RuntimeException {

    private final Map<String, Object> details;

    public MoplException(String message) {
        this(message, null);
    }

    public MoplException(
        String message,
        Map<String, Object> details
    ) {
        super(message);

        this.details = details != null
            ? Collections.unmodifiableMap(details)
            : Collections.emptyMap();
    }
}
