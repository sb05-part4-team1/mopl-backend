package com.mopl.domain.exception.storage;

import com.mopl.domain.exception.ErrorCode;

import java.util.Map;

public class FileUploadException extends StorageException {

    private static final ErrorCode ERROR_CODE = StorageErrorCode.FILE_UPLOAD_FAILED;

    private FileUploadException(Map<String, Object> details) {
        super(ERROR_CODE, details);
    }

    public static FileUploadException withPath(String path) {
        return new FileUploadException(Map.of("path", path));
    }

    public static FileUploadException withPathAndCause(String path, String cause) {
        return new FileUploadException(Map.of("path", path, "cause", cause));
    }
}
