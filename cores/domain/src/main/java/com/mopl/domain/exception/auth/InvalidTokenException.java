package com.mopl.domain.exception.auth;

import java.util.Map;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
