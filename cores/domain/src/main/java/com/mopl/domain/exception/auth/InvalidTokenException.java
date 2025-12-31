package com.mopl.domain.exception.auth;

import java.util.Map;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException() {
        super(AuthErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String detailMessage) {
        super(AuthErrorCode.INVALID_TOKEN, Map.of("detailMessage", detailMessage));
    }
}
