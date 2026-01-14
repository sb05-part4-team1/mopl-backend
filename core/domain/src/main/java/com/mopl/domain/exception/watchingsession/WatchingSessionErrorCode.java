package com.mopl.domain.exception.watchingsession;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WatchingSessionErrorCode implements ErrorCode {

    INVALID_WATCHING_SESSION_DATA(400, "유효하지 않은 시청 세션 데이터입니다"),
    WATCHING_SESSION_NOT_FOUND(404, "시청 세션을 찾을 수 없습니다");

    private final int status;
    private final String message;
}
