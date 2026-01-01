package com.mopl.domain.exception.user;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class UserException extends MoplException {

    protected UserException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
