package com.mopl.domain.exception.content;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class ContentException extends MoplException {

    protected ContentException(String message) {
        super(message);
    }

    protected ContentException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
