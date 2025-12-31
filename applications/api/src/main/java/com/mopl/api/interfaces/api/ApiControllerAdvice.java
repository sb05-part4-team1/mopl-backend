package com.mopl.api.interfaces.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import com.mopl.domain.exception.ErrorResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiControllerAdvice {

    // --- 1. 라우팅 ---

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleEndPointNotFoundException(
        Exception exception
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, "존재하지 않는 API 엔드포인트입니다.", Map.of());
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

        return buildResponse(
            HttpStatus.METHOD_NOT_ALLOWED,
            exception, "허용되지 않는 HTTP 메서드입니다.",
            details
        );
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

        return buildResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            exception,
            "지원하지 않는 Content-Type입니다.",
            details
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException exception
    ) {
        return buildResponse(HttpStatus.NOT_ACCEPTABLE, exception, "허용되지 않는 Accept 헤더입니다.", Map.of(
            "supportedMediaTypes", exception.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .toList()
        ));
    }

    // --- 2. 파라미터 바인딩 ---

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "필수 요청 매개변수가 누락되었습니다.", Map.of(
            "parameter", exception.getParameterName(),
            "expectedType", exception.getParameterType()
        ));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
        MissingServletRequestPartException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "필수 요청 파트가 누락되었습니다.", Map.of(
            "part", exception.getRequestPartName()
        ));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
        MissingRequestCookieException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "필수 쿠키가 누락되었습니다.", Map.of(
            "cookie", exception.getCookieName()
        ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
        MissingRequestHeaderException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "필수 헤더가 누락되었습니다.", Map.of(
            "header", exception.getHeaderName()
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException exception
    ) {
        String expectedType = exception.getRequiredType() != null
            ? exception.getRequiredType().getSimpleName()
            : "unknown";

        return buildResponse(HttpStatus.BAD_REQUEST, exception, "요청 매개변수 타입이 유효하지 않습니다.", Map.of(
            "parameter", exception.getName(),
            "type", exception.getValue() != null
                ? exception.getValue().getClass().getSimpleName()
                : "UNKNOWN",
            "expectedType", expectedType
        ));
    }

    // --- 3. 바디 파싱 ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "요청 본문을 읽을 수 없습니다.", Map.of());
    }

    // --- 4. 검증 ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", extractFieldErrors(exception.getBindingResult()));
        details.put("globalErrors", extractGlobalErrors(exception.getBindingResult()));

        return buildResponse(HttpStatus.BAD_REQUEST, exception, "요청 본문 값이 유효하지 않습니다.", details);
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

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception,
            "요청 매개변수 값이 유효하지 않습니다.",
            Map.of("violations", violations)
        );
    }

    // --- 5. 인가 ---

    // Security 관련 예외 핸들러는 추후 구현합니다.
    // @ExceptionHandler(AuthorizationDeniedException.class)
    // public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
    //     AuthorizationDeniedException exception
    // ) {
    //     return buildResponse(HttpStatus.FORBIDDEN, exception, "접근 권한이 없습니다.", Map.of());
    // }

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

        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "InternalServerError",
            "서버 오류가 발생했습니다.",
            Map.of()
        );
    }

    // --- Helper Methods ---

    private ResponseEntity<ErrorResponse> buildResponse(
        HttpStatus status,
        Exception exception,
        String message,
        Map<String, Object> details
    ) {
        return buildResponse(status, exception.getClass().getSimpleName(), message, details);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
        HttpStatus status,
        String exceptionName,
        String message,
        Map<String, Object> details
    ) {
        return ResponseEntity
            .status(status)
            .body(ErrorResponse.of(exceptionName, message, details));
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
