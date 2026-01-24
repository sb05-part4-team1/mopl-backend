package com.mopl.domain.exception.storage;

import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

public abstract class StorageException extends MoplException {

    protected StorageException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
