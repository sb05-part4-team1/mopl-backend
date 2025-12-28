package com.mopl.security.jwt;

import com.mopl.security.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieProvider {

    private static final int EXPIRED = 0;

    private final String refreshTokenCookieName;
    private final int refreshTokenMaxAge;

    public JwtCookieProvider(JwtProperties jwtProperties) {
        this.refreshTokenCookieName = jwtProperties.refreshTokenCookieName();
        this.refreshTokenMaxAge = (int) jwtProperties.refreshToken().expiration().toSeconds();
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(refreshToken, refreshTokenMaxAge);
    }

    public Cookie createExpiredRefreshTokenCookie() {
        return createCookie("", EXPIRED);
    }

    private Cookie createCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(refreshTokenCookieName, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
