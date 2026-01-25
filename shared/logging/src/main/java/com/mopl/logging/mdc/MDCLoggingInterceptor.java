package com.mopl.logging.mdc;

import static com.mopl.logging.mdc.MDCKeys.REQUEST_ID;
import static com.mopl.logging.mdc.MDCKeys.REQUEST_METHOD;
import static com.mopl.logging.mdc.MDCKeys.REQUEST_URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

public class MDCLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        @NonNull Object handler
    ) {
        // 1. requestId 생성
        String requestId = UUID.randomUUID().toString();

        // 2. MDC 저장
        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_METHOD, request.getMethod());
        MDC.put(REQUEST_URI, request.getRequestURI());

        // 3. 응답 헤더에도 내려주기 (추적용)
        response.setHeader("X-Request-Id", requestId);

        return true;
    }

    @Override
    public void afterCompletion(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler,
        Exception ex
    ) {
        // 반드시 호출
        MDC.clear();
    }

}
