package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.exception.MoplException;
import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidCredentialsException;
import com.mopl.security.exception.ApiResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class SignInFailureHandler implements AuthenticationFailureHandler {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        log.warn("로그인 실패: email={}, message={}",
            request.getParameter("email"),
            exception.getMessage()
        );

        MoplException domainException = convertToDomainException(exception);

        apiResponseHandler.writeError(response, domainException);
    }

    private MoplException convertToDomainException(AuthenticationException exception) {
        if (exception instanceof LockedException) {
            return new AccountLockedException();
        }
        if (exception instanceof AuthenticationServiceException
            || exception instanceof ProviderNotFoundException) {
            return new InternalServerException();
        }
        return new InvalidCredentialsException();
    }
}
