package com.mopl.domain.exception;

public class InternalServerException extends MoplException {

    private static final String MESSAGE = "서버 내부 오류가 발생했습니다.";

    public InternalServerException() {
        super(MESSAGE);
    }
}
