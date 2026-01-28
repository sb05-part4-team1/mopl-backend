package com.mopl.security.jwt.provider;

import com.mopl.security.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtCookieProvider 단위 테스트")
class JwtCookieProviderTest {

    private JwtCookieProvider cookieProvider;

    private static final String COOKIE_NAME = "REFRESH_TOKEN";
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config("access-secret-key-for-testing-32-bytes", Duration.ofMinutes(30), null),
            new JwtProperties.Config("refresh-secret-key-for-testing-32-bytes", REFRESH_EXPIRATION, null),
            3,
            JwtProperties.JwtRegistryType.IN_MEMORY,
            COOKIE_NAME
        );
        cookieProvider = new JwtCookieProvider(jwtProperties);
    }

    @Nested
    @DisplayName("createRefreshTokenCookie()")
    class CreateRefreshTokenCookieTest {

        @Test
        @DisplayName("리프레시 토큰으로 쿠키를 생성한다")
        void withRefreshToken_createsCookie() {
            // given
            String refreshToken = "test-refresh-token";

            // when
            Cookie cookie = cookieProvider.createRefreshTokenCookie(refreshToken);

            // then
            assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
            assertThat(cookie.getValue()).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("쿠키는 HttpOnly로 설정된다")
        void cookie_isHttpOnly() {
            // when
            Cookie cookie = cookieProvider.createRefreshTokenCookie("token");

            // then
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("쿠키는 Secure로 설정된다")
        void cookie_isSecure() {
            // when
            Cookie cookie = cookieProvider.createRefreshTokenCookie("token");

            // then
            assertThat(cookie.getSecure()).isTrue();
        }

        @Test
        @DisplayName("쿠키 경로는 /api/auth로 설정된다")
        void cookie_hasAuthPath() {
            // when
            Cookie cookie = cookieProvider.createRefreshTokenCookie("token");

            // then
            assertThat(cookie.getPath()).isEqualTo("/api/auth");
        }

        @Test
        @DisplayName("쿠키 만료 시간은 리프레시 토큰 만료 시간과 동일하다")
        void cookie_hasCorrectMaxAge() {
            // when
            Cookie cookie = cookieProvider.createRefreshTokenCookie("token");

            // then
            assertThat(cookie.getMaxAge()).isEqualTo((int) REFRESH_EXPIRATION.toSeconds());
        }
    }

    @Nested
    @DisplayName("createExpiredRefreshTokenCookie()")
    class CreateExpiredRefreshTokenCookieTest {

        @Test
        @DisplayName("만료된 쿠키를 생성한다")
        void createsExpiredCookie() {
            // when
            Cookie cookie = cookieProvider.createExpiredRefreshTokenCookie();

            // then
            assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge()).isZero();
        }

        @Test
        @DisplayName("만료된 쿠키도 HttpOnly로 설정된다")
        void expiredCookie_isHttpOnly() {
            // when
            Cookie cookie = cookieProvider.createExpiredRefreshTokenCookie();

            // then
            assertThat(cookie.isHttpOnly()).isTrue();
        }

        @Test
        @DisplayName("만료된 쿠키도 Secure로 설정된다")
        void expiredCookie_isSecure() {
            // when
            Cookie cookie = cookieProvider.createExpiredRefreshTokenCookie();

            // then
            assertThat(cookie.getSecure()).isTrue();
        }

        @Test
        @DisplayName("만료된 쿠키도 동일한 경로를 가진다")
        void expiredCookie_hasSamePath() {
            // when
            Cookie cookie = cookieProvider.createExpiredRefreshTokenCookie();

            // then
            assertThat(cookie.getPath()).isEqualTo("/api/auth");
        }
    }
}
