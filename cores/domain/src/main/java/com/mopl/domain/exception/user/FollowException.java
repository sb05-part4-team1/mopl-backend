package com.mopl.domain.exception.user;

import java.util.Map;

import com.mopl.domain.exception.MoplException;

public abstract class FollowException extends MoplException {

    protected FollowException(String message) {
        super(message);
    }

    protected FollowException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
