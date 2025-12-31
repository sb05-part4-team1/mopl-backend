package com.mopl.security.exception;

import com.mopl.domain.exception.auth.InsufficientRoleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("Access denied: {}", accessDeniedException.getMessage());
        apiResponseHandler.writeError(response, new InsufficientRoleException());
    }
}
