package com.mopl.domain.exception.content;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class ContentException extends MoplException {

    protected ContentException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected ContentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
