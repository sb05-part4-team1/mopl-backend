package com.mopl.domain.exception.content;

import com.mopl.domain.exception.user.UserException;

import java.util.Map;

public class InvalidContentDataException extends UserException {

    public static final String MESSAGE = "콘텐츠 데이터가 유효하지 않습니다.";

    public InvalidContentDataException(String detailMessage) {
        super(MESSAGE, Map.of("detailMessage", detailMessage));
    }
}
