package com.mopl.domain.exception;

public class InternalServerException extends MoplException {

    public InternalServerException() {
        super(ApiErrorCode.INTERNAL_SERVER_ERROR);
    }
}
