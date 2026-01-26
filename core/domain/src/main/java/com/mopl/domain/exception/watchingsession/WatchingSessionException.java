package com.mopl.domain.exception.watchingsession;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class WatchingSessionException extends MoplException {

    protected WatchingSessionException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
