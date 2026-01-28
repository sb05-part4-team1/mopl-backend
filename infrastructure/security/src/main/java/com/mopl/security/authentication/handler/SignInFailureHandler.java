package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.InternalServerException;
import com.mopl.domain.exception.MoplException;
import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidCredentialsException;
import com.mopl.logging.context.LogContext;
import com.mopl.security.exception.ApiResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class SignInFailureHandler implements AuthenticationFailureHandler {

    private final ApiResponseHandler apiResponseHandler;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        String email = request.getParameter("email");

        LogContext.with("email", email).and("reason", exception.getMessage()).warn("Sign-in failed");

        MoplException domainException = convertToDomainException(exception, email);

        apiResponseHandler.writeError(response, domainException);
    }

    private MoplException convertToDomainException(AuthenticationException exception, String email) {
        return switch (exception) {
            case LockedException ignored -> AccountLockedException.withEmail(email);
            case AuthenticationServiceException ignored -> InternalServerException.create();
            case ProviderNotFoundException ignored -> InternalServerException.create();
            default -> InvalidCredentialsException.create();
        };
    }
}
