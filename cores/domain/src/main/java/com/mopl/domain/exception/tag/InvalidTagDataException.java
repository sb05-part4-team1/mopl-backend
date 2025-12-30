package com.mopl.domain.exception.tag;

import java.util.Map;

public class InvalidTagDataException extends TagException {

    public static final String MESSAGE = "태그 데이터가 유효하지 않습니다.";

    public InvalidTagDataException(String detailMessage) {
        super(MESSAGE, Map.of("detailMessage", detailMessage));
    }
}
