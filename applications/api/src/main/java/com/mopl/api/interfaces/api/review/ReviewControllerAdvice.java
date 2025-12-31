package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.ErrorResponse;
import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
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

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFoundException(
        ReviewNotFoundException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(ReviewForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleReviewForbiddenException(
        ReviewForbiddenException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
        MissingRequestHeaderException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED) // 401로 Swagger에 맞춤
            .body(ErrorResponse.of(
                exception.getClass().getSimpleName(),
                "인증이 필요합니다.",
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
