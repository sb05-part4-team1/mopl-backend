package com.mopl.domain.exception.watchingsession;

import java.util.Map;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

public abstract class WatchingSessionException extends MoplException {

    protected WatchingSessionException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected WatchingSessionException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
