package com.mopl.domain.exception.storage;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class FileNotFoundException extends StorageException {

    private static final ErrorCode ERROR_CODE = StorageErrorCode.FILE_NOT_FOUND;

    private FileNotFoundException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FileNotFoundException withPath(String path) {
        return new FileNotFoundException(Map.of("path", path));
    }
}
