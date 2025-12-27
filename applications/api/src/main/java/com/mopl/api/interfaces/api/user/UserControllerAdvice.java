package com.mopl.api.interfaces.api.user;

import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.exception.user.UserNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice(basePackages = "com.mopl.api.interfaces.api.user")
public class UserControllerAdvice {

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserDataException(
        InvalidUserDataException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
        UserNotFoundException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
        DuplicateEmailException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.from(exception));
    }
}
