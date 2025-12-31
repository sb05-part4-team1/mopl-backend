package com.mopl.domain.exception.review;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class ReviewException extends MoplException {

    protected ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected ReviewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
