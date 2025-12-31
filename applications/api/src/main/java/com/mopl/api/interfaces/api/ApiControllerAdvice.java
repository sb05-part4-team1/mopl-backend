package com.mopl.api.interfaces.api;

import com.mopl.domain.exception.ApiErrorCode;
import com.mopl.domain.exception.ErrorCode;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.MoplException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiControllerAdvice {

    // --- 1. 라우팅 ---

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleEndPointNotFoundException(Exception exception) {
        return buildResponse(ApiErrorCode.ENDPOINT_NOT_FOUND, exception, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException exception
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("method", exception.getMethod());
        details.put("supportedMethods", exception.getSupportedHttpMethods() != null
            ? exception.getSupportedHttpMethods().stream().map(HttpMethod::name).toList()
            : List.of());

        return buildResponse(ApiErrorCode.METHOD_NOT_ALLOWED, exception, details);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException exception
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("contentType", exception.getContentType() != null
            ? exception.getContentType().toString()
            : "UNKNOWN");
        details.put("supportedMediaTypes", exception.getSupportedMediaTypes().stream()
            .map(MediaType::toString)
            .toList());

        return buildResponse(ApiErrorCode.UNSUPPORTED_MEDIA_TYPE, exception, details);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException exception
    ) {
        Map<String, Object> details = Map.of(
            "supportedMediaTypes", exception.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .toList()
        );
        return buildResponse(ApiErrorCode.NOT_ACCEPTABLE, exception, details);
    }

    // --- 2. 파라미터 바인딩 ---

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException exception
    ) {
        Map<String, Object> details = Map.of(
            "parameter", exception.getParameterName(),
            "expectedType", exception.getParameterType()
        );
        return buildResponse(ApiErrorCode.MISSING_PARAMETER, exception, details);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
        MissingServletRequestPartException exception
    ) {
        Map<String, Object> details = Map.of(
            "part", exception.getRequestPartName()
        );
        return buildResponse(ApiErrorCode.MISSING_PART, exception, details);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
        MissingRequestCookieException exception
    ) {
        Map<String, Object> details = Map.of(
            "cookie", exception.getCookieName()
        );
        return buildResponse(ApiErrorCode.MISSING_COOKIE, exception, details);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
        MissingRequestHeaderException exception
    ) {
        Map<String, Object> details = Map.of(
            "header", exception.getHeaderName()
        );
        return buildResponse(ApiErrorCode.MISSING_HEADER, exception, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException exception
    ) {
        String expectedType = exception.getRequiredType() != null
            ? exception.getRequiredType().getSimpleName()
            : "unknown";
        Map<String, Object> details = Map.of(
            "parameter", exception.getName(),
            "type", exception.getValue() != null
                ? exception.getValue().getClass().getSimpleName()
                : "UNKNOWN",
            "expectedType", expectedType
        );
        return buildResponse(ApiErrorCode.INVALID_PARAMETER_TYPE, exception, details);
    }

    // --- 3. 바디 파싱 ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException exception
    ) {
        return buildResponse(ApiErrorCode.MESSAGE_NOT_READABLE, exception, Map.of());
    }

    // --- 4. 검증 ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", extractFieldErrors(exception.getBindingResult()));
        details.put("globalErrors", extractGlobalErrors(exception.getBindingResult()));

        return buildResponse(ApiErrorCode.INVALID_REQUEST_BODY, exception, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException exception
    ) {
        List<FieldErrorDetail> violations = exception.getConstraintViolations().stream()
            .map(cv -> {
                String propertyPath = cv.getPropertyPath().toString();
                String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                return new FieldErrorDetail(field, cv.getMessage());
            })
            .toList();

        Map<String, Object> details = Map.of("violations", violations);

        return buildResponse(ApiErrorCode.INVALID_PARAMETER_VALUE, exception, details);
    }

    // --- 5. 비즈니스 예외 ---

    @ExceptionHandler(MoplException.class)
    public ResponseEntity<ErrorResponse> handleMoplException(MoplException exception) {
        return ResponseEntity
            .status(exception.getErrorCode().getStatus())
            .body(ErrorResponse.from(exception));
    }

    // --- 6. 서버 에러 ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error("[{}] {} {} :{}",
            exception.getClass().getSimpleName(),
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage(),
            exception
        );

        return buildResponse(ApiErrorCode.INTERNAL_SERVER_ERROR, "InternalServerError", Map.of());
    }

    // --- Helper Methods ---

    private ResponseEntity<ErrorResponse> buildResponse(
        ErrorCode errorCode,
        Exception exception,
        Map<String, Object> details
    ) {
        return buildResponse(errorCode, exception.getClass().getSimpleName(), details);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
        ErrorCode errorCode,
        String exceptionName,
        Map<String, Object> details
    ) {
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(exceptionName, errorCode.getMessage(), details));
    }

    private List<FieldErrorDetail> extractFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
            .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
            .toList();
    }

    private List<GlobalErrorDetail> extractGlobalErrors(BindingResult bindingResult) {
        return bindingResult.getGlobalErrors().stream()
            .map(e -> new GlobalErrorDetail(e.getObjectName(), e.getDefaultMessage()))
            .toList();
    }

    // --- DTOs ---

    record FieldErrorDetail(String field, String message) {
    }

    record GlobalErrorDetail(String object, String message) {
    }
}
