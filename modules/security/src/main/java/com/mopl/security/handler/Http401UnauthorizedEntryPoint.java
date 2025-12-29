package com.mopl.security.handler;

import com.mopl.domain.exception.auth.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        apiResponseHandler.writeError(response, new UnauthorizedException());
    }
}
