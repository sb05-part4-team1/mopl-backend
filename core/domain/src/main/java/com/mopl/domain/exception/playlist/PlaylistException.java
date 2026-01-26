package com.mopl.domain.exception.playlist;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class PlaylistException extends MoplException {

    protected PlaylistException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
