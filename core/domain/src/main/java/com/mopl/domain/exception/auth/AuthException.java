package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class AuthException extends MoplException {

    protected AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected AuthException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
