package com.mopl.domain.exception.user;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class UserException extends MoplException {

    protected UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
