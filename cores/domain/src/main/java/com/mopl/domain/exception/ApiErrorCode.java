package com.mopl.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode implements ErrorCode {

    // 라우팅
    ENDPOINT_NOT_FOUND(404, "존재하지 않는 API 엔드포인트입니다"),
    METHOD_NOT_ALLOWED(405, "허용되지 않는 HTTP 메서드입니다"),
    NOT_ACCEPTABLE(406, "허용되지 않는 Accept 헤더입니다"),
    UNSUPPORTED_MEDIA_TYPE(415, "지원하지 않는 Content-Type입니다"),

    // 파라미터 바인딩
    MISSING_PARAMETER(400, "필수 요청 매개변수가 누락되었습니다"),
    MISSING_PART(400, "필수 요청 파트가 누락되었습니다"),
    MISSING_COOKIE(400, "필수 쿠키가 누락되었습니다"),
    MISSING_HEADER(400, "필수 헤더가 누락되었습니다"),
    INVALID_PARAMETER_TYPE(400, "요청 매개변수 타입이 유효하지 않습니다"),

    // 바디 파싱
    MESSAGE_NOT_READABLE(400, "요청 본문을 읽을 수 없습니다"),

    // 검증
    INVALID_REQUEST_BODY(400, "요청 본문 값이 유효하지 않습니다"),
    INVALID_PARAMETER_VALUE(400, "요청 매개변수 값이 유효하지 않습니다"),

    // 서버 에러
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다");

    private final int status;
    private final String message;
}
