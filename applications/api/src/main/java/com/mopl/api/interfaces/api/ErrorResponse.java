package com.mopl.api.interfaces.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mopl.domain.exception.MoplException;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    public static ErrorResponse from(MoplException exception) {
        return new ErrorResponse(
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            exception.getDetails()
        );
    }
}
