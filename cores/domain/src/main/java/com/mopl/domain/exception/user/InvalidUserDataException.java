package com.mopl.domain.exception.user;

import java.util.Map;

public class InvalidUserDataException extends UserException {

    public static final String MESSAGE = "사용자 데이터가 유효하지 않습니다.";

    public InvalidUserDataException(String detailMessage) {
        super(MESSAGE, Map.of("detailMessage", detailMessage));
    }
}
