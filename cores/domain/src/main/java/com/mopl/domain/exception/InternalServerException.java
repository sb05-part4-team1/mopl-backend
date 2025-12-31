package com.mopl.domain.exception;

public class InternalServerException extends MoplException {

    public InternalServerException() {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
