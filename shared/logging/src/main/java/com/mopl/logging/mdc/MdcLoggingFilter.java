package com.mopl.logging.mdc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.mopl.logging.mdc.MdcKeys.HEADER_REQUEST_ID;
import static com.mopl.logging.mdc.MdcKeys.HEADER_TRACE_ID;
import static com.mopl.logging.mdc.MdcKeys.IP_ADDRESS;
import static com.mopl.logging.mdc.MdcKeys.REQUEST_ID;
import static com.mopl.logging.mdc.MdcKeys.REQUEST_METHOD;
import static com.mopl.logging.mdc.MdcKeys.REQUEST_START_TIME;
import static com.mopl.logging.mdc.MdcKeys.REQUEST_URI;
import static com.mopl.logging.mdc.MdcKeys.TRACE_ID;
import static com.mopl.logging.mdc.MdcKeys.USER_AGENT;
import static com.mopl.logging.mdc.MdcKeys.USER_ID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = extractOrGenerateId(request, HEADER_REQUEST_ID);
        String traceId = extractOrGenerateId(request, HEADER_TRACE_ID);
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String requestStartTime = String.valueOf(System.currentTimeMillis());
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(TRACE_ID, traceId);
        MDC.put(REQUEST_METHOD, requestMethod);
        MDC.put(REQUEST_URI, requestUri);
        MDC.put(REQUEST_START_TIME, requestStartTime);
        MDC.put(IP_ADDRESS, ipAddress);
        MDC.put(USER_AGENT, userAgent);

        response.setHeader(HEADER_REQUEST_ID, requestId);
        response.setHeader(HEADER_TRACE_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequestCompletion(response);
            clearMdcContext();
        }
    }

    private String extractOrGenerateId(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.isEmpty()) {
            return headerValue;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }
        if (userAgent.length() > 100) {
            return userAgent.substring(0, 100);
        }
        return userAgent;
    }

    private void logRequestCompletion(HttpServletResponse response) {
        String requestMethod = MDC.get(REQUEST_METHOD);
        String requestUri = MDC.get(REQUEST_URI);
        int status = response.getStatus();
        String duration = calculateDuration();

        if (status >= 500) {
            log.error("{} {} [status={}, duration={}]",
                requestMethod, requestUri, status, duration);
        } else if (status >= 400) {
            log.warn("{} {} [status={}, duration={}]",
                requestMethod, requestUri, status, duration);
        } else {
            log.info("{} {} [status={}, duration={}]",
                requestMethod, requestUri, status, duration);
        }
    }

    private String calculateDuration() {
        String startTimeStr = MDC.get(REQUEST_START_TIME);
        if (startTimeStr == null) {
            return "N/A";
        }
        try {
            long duration = System.currentTimeMillis() - Long.parseLong(startTimeStr);
            return duration + "ms";
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

    private void clearMdcContext() {
        MDC.remove(REQUEST_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(REQUEST_METHOD);
        MDC.remove(REQUEST_URI);
        MDC.remove(REQUEST_START_TIME);
        MDC.remove(USER_ID);
        MDC.remove(IP_ADDRESS);
        MDC.remove(USER_AGENT);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/favicon");
    }
}
