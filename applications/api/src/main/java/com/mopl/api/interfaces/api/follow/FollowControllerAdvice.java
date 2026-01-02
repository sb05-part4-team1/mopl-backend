package com.mopl.api.interfaces.api.follow;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.mopl.domain.exception.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mopl.domain.exception.follow.SelfFollowException;

@Order(1)
@RestControllerAdvice(basePackages = "com.mopl.api.interfaces.api.user")
public class FollowControllerAdvice {

    @ExceptionHandler(SelfFollowException.class)
    public ResponseEntity<ErrorResponse> handleSelfFollowException(
        SelfFollowException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(exception));
    }
}
