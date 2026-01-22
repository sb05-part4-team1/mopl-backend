package com.mopl.domain.exception.follow;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class FollowException extends MoplException {

    protected FollowException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
