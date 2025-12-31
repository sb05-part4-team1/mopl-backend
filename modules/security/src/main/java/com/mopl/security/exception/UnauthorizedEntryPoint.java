package com.mopl.security.exception;

import com.mopl.domain.exception.auth.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authenticationException
    ) throws IOException {
        log.debug("Unauthorized access: {}", authenticationException.getMessage());
        apiResponseHandler.writeError(response, new UnauthorizedException());
    }
}
