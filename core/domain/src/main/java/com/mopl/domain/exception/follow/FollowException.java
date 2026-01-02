package com.mopl.domain.exception.follow;

import java.util.Map;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

public abstract class FollowException extends MoplException {

    protected FollowException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
