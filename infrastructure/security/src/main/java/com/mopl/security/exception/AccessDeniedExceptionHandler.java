package com.mopl.security.exception;

import com.mopl.domain.exception.auth.InsufficientRoleException;
import com.mopl.logging.context.LogContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        LogContext.with("reason", accessDeniedException.getMessage()).warn("Access denied");
        apiResponseHandler.writeError(response, InsufficientRoleException.create());
    }
}
