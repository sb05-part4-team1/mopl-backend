package com.mopl.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.exception.ErrorResponse;
import com.mopl.domain.exception.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authenticationException
    ) throws IOException {
        log.error("로그인 실패: {}", authenticationException.getMessage(), authenticationException);
        String email = request.getParameter("email");
        InvalidCredentialsException exception = new InvalidCredentialsException(email);
        ErrorResponse errorResponse = ErrorResponse.from(exception);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
