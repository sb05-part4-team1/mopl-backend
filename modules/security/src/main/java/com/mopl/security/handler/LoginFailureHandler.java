package com.mopl.security.handler;

import com.mopl.domain.exception.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        log.warn("로그인 실패: {}, Message: {}",
            request.getParameter("email"),
            exception.getMessage()
        );

        InvalidCredentialsException businessException = new InvalidCredentialsException();

        apiResponseHandler.writeError(response, businessException);
    }
}
