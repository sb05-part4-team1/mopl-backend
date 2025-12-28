package com.mopl.security.handler.jwt;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.provider.jwt.JwtCookieProvider;
import com.mopl.security.provider.jwt.JwtProvider;
import com.mopl.security.provider.jwt.registry.JwtRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${mopl.jwt.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    private final JwtProvider jwtProvider;
    private final JwtCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        Cookie refreshTokenCookie = WebUtils.getCookie(request, refreshTokenCookieName);

        if (refreshTokenCookie != null) {
            invalidate(refreshTokenCookie.getValue(), request);
        }

        response.addCookie(cookieProvider.createExpiredRefreshTokenCookie());
        log.debug("리프레시 토큰 쿠키 삭제 완료");
    }

    private void invalidate(String refreshToken, HttpServletRequest request) {
        try {
            JwtPayload jwtPayload = jwtProvider.verifyRefreshToken(refreshToken);

            // 이거 revoke 따로따로 하자
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            JwtPayload jwtPayload = jwtProvider.verifyAccessToken(token);
            if (!jwtRegistry.isAccessTokenBlacklisted(jwtPayload.jti())) {
                log.debug("유효하지 않은 JWT 토큰");
                responseWriter.writeError(response, new InvalidTokenException("유효하지 않은 토큰입니다."));
                return;
            }

            log.debug("JWT 로그아웃 완료: userId={}", userId);
        } catch (Exception e) {
            log.debug("리프레시 토큰 파싱 실패: {}", e.getMessage());
        }
    }
}
