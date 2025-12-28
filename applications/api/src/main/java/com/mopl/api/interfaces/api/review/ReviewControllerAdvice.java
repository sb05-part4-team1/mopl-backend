package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.ErrorResponse;
import com.mopl.domain.exception.review.InvalidReviewDataException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(1)
@RestControllerAdvice(basePackages = "com.mopl.api.interfaces.api.review")
public class ReviewControllerAdvice {

    @ExceptionHandler(InvalidReviewDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReviewDataException(
        InvalidReviewDataException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
        MissingRequestHeaderException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                exception.getClass().getSimpleName(),
                "필수 헤더가 누락되었습니다.",
                java.util.Map.of("header", exception.getHeaderName())
            ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException exception
    ) {
        String expectedType = exception.getRequiredType() != null
            ? exception.getRequiredType().getSimpleName()
            : "unknown";

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                exception.getClass().getSimpleName(),
                "요청 매개변수 타입이 유효하지 않습니다.",
                java.util.Map.of(
                    "parameter", exception.getName(),
                    "value", exception.getValue() != null ? exception.getValue().toString()
                        : "UNKNOWN",
                    "expectedType", expectedType
                )
            ));
    }
}
