package com.mopl.domain.exception.outbox;

import com.mopl.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxErrorCode implements ErrorCode {

    INVALID_OUTBOX_DATA(400, "유효하지 않은 Outbox 데이터입니다."),
    EVENT_SERIALIZATION_FAILED(500, "이벤트 직렬화에 실패했습니다.");

    private final int status;
    private final String message;
}
