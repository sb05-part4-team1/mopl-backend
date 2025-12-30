package com.mopl.api.interfaces.api.content;

import com.mopl.api.interfaces.api.ErrorResponse;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.exception.tag.InvalidTagDataException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice(basePackages = "com.mopl.api.interfaces.api.content")
public class ContentControllerAdvice {

    @ExceptionHandler(InvalidContentDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidContent(
        InvalidContentDataException exception) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(InvalidTagDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTag(InvalidTagDataException exception) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContentNotFound(ContentNotFoundException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(exception));
    }
}
