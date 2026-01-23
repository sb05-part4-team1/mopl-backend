package com.mopl.domain.exception.storage;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class FileDeleteException extends StorageException {

    private static final ErrorCode ERROR_CODE = StorageErrorCode.FILE_DELETE_FAILED;

    private FileDeleteException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FileDeleteException withPath(String path) {
        return new FileDeleteException(Map.of("path", path));
    }

    public static FileDeleteException withPathAndCause(String path, String cause) {
        return new FileDeleteException(Map.of("path", path, "cause", cause));
    }
}
