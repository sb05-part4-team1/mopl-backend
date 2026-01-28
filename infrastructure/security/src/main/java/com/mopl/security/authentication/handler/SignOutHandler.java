package com.mopl.security.authentication.handler;

import com.mopl.logging.context.LogContext;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.util.WebUtils;

public class SignOutHandler implements LogoutHandler {

    private static final String BEARER_PREFIX = "Bearer ";

    private final String refreshTokenCookieName;
    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;

    public SignOutHandler(
        JwtProvider jwtProvider,
        JwtCookieProvider cookieProvider,
        JwtRegistry jwtRegistry,
        JwtProperties jwtProperties
    ) {
        this.jwtProvider = jwtProvider;
        this.cookieProvider = cookieProvider;
        this.jwtRegistry = jwtRegistry;
        this.refreshTokenCookieName = jwtProperties.refreshTokenCookieName();
    }

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        revokeAccessToken(request);
        revokeRefreshToken(request);
        response.addCookie(cookieProvider.createExpiredRefreshTokenCookie());
    }

    private void revokeAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            JwtPayload payload = jwtProvider.verifyAndParse(token, TokenType.ACCESS);
            jwtRegistry.revokeAccessToken(payload.jti(), payload.exp());
            LogContext.with("jti", payload.jti()).debug("Access token revoked");
        } catch (Exception e) {
            LogContext.with("reason", e.getMessage()).debug("Access token revocation failed");
        }
    }

    private void revokeRefreshToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);
        if (cookie == null) {
            return;
        }

        try {
            JwtPayload payload = jwtProvider.verifyAndParse(cookie.getValue(), TokenType.REFRESH);
            jwtRegistry.revokeRefreshToken(payload.sub(), payload.jti());
            LogContext.with("userId", payload.sub()).and("jti", payload.jti()).debug("Refresh token revoked");
        } catch (Exception e) {
            LogContext.with("reason", e.getMessage()).debug("Refresh token revocation failed");
        }
    }
}
