package com.mopl.domain.exception.storage;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(500, "파일 삭제에 실패했습니다."),
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다.");

    private final int status;
    private final String message;
}
