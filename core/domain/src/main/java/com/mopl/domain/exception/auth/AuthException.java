package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class AuthException extends MoplException {

    protected AuthException(String message) {
        super(message);
    }

    protected AuthException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
