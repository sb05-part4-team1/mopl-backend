package com.mopl.domain.exception.auth;

import com.mopl.domain.exception.MoplException;

public abstract class AuthException extends MoplException {

    protected AuthException(String message) {
        super(message);
    }
}
