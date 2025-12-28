package com.mopl.domain.exception.tag;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class TagException extends MoplException {

    protected TagException(String message) {
        super(message);
    }

    protected TagException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
