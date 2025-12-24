package com.mopl.api.interfaces.api;

import com.mopl.domain.exception.MoplException;

import java.util.Map;

public record ErrorResponse(
    String exceptionName,
    String message,
    Map<String, Object> details
) {
    public static ErrorResponse of(
        String exceptionName,
        String message,
        Map<String, Object> details
    ) {
        return new ErrorResponse(
            exceptionName,
            message,
            details
        );
    }

    public static ErrorResponse from(MoplException excpetion) {
        return new ErrorResponse(
            excpetion.getClass().getSimpleName(),
            excpetion.getMessage(),
            excpetion.getDetails()
        );
    }
}
