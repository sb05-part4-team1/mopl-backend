package com.mopl.security.jwt.provider;

import com.mopl.security.config.JwtProperties;
import jakarta.servlet.http.Cookie;

import java.time.Duration;

public class JwtCookieProvider {

    private static final String AUTH_PATH = "/api/auth";

    private final String refreshTokenCookieName;
    private final Duration refreshTokenExpiration;

    public JwtCookieProvider(JwtProperties jwtProperties) {
        this.refreshTokenCookieName = jwtProperties.refreshTokenCookieName();
        this.refreshTokenExpiration = jwtProperties.refreshToken().expiration();
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(AUTH_PATH);
        cookie.setMaxAge((int) refreshTokenExpiration.toSeconds());
        return cookie;
    }

    public Cookie createExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie(refreshTokenCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(AUTH_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }
}
