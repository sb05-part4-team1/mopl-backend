package com.mopl.domain.exception;

public class InternalServerException extends MoplException {

    private static final ErrorCode ERROR_CODE = ApiErrorCode.INTERNAL_SERVER_ERROR;

    private InternalServerException() {
        super(ERROR_CODE);
    }

    public static InternalServerException create() {
        return new InternalServerException();
    }
}
