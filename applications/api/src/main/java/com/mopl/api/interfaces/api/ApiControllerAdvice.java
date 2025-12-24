package com.mopl.api.interfaces.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<?> handle(Throwable e) {
        log.error("Exception : {}", e.getMessage(), e);
        return null;
    }
}
