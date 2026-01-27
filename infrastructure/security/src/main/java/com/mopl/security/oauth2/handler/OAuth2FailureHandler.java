package com.mopl.security.oauth2.handler;

import com.mopl.logging.context.LogContext;
import com.mopl.security.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2Properties oAuth2Properties;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        LogContext.with("reason", exception.getMessage()).warn("OAuth2 sign-in failed");

        String redirectUrl = UriComponentsBuilder
            .fromUriString(oAuth2Properties.frontendRedirectUri())
            .path("/#/sign-in")
            .build()
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
