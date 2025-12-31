package com.mopl.domain.exception.tag;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class TagException extends MoplException {

    protected TagException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected TagException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
