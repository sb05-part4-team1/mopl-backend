package com.mopl.security.authentication.handler;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignOutHandler 단위 테스트")
class SignOutHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtCookieProvider cookieProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private Authentication authentication;

    private SignOutHandler signOutHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String COOKIE_NAME = "REFRESH_TOKEN";

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config("access-secret-key-for-testing-32-bytes", Duration.ofMinutes(30), null),
            new JwtProperties.Config("refresh-secret-key-for-testing-32-bytes", Duration.ofDays(7), null),
            3,
            JwtProperties.JwtRegistryType.IN_MEMORY,
            COOKIE_NAME
        );

        signOutHandler = new SignOutHandler(jwtProvider, cookieProvider, jwtRegistry, jwtProperties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("logout()")
    class LogoutTest {

        @Test
        @DisplayName("액세스 토큰과 리프레시 토큰을 모두 무효화한다")
        void revokesAllTokens() {
            // given
            String accessToken = "access-token";
            String refreshToken = "refresh-token";
            UUID userId = UUID.randomUUID();
            UUID accessJti = UUID.randomUUID();
            UUID refreshJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() + 3600_000);

            JwtPayload accessPayload = new JwtPayload(userId, accessJti, new Date(), expiration, UserModel.Role.USER);
            JwtPayload refreshPayload = new JwtPayload(userId, refreshJti, new Date(), expiration, UserModel.Role.USER);

            request.addHeader("Authorization", "Bearer " + accessToken);
            request.setCookies(new Cookie(COOKIE_NAME, refreshToken));

            given(jwtProvider.verifyAndParse(accessToken, TokenType.ACCESS)).willReturn(accessPayload);
            given(jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH)).willReturn(refreshPayload);
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtRegistry).should().revokeAccessToken(accessJti, expiration);
            then(jwtRegistry).should().revokeRefreshToken(userId, refreshJti);
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 액세스 토큰 무효화를 건너뛴다")
        void withoutAuthHeader_skipsAccessTokenRevocation() {
            // given
            String refreshToken = "refresh-token";
            UUID userId = UUID.randomUUID();
            UUID refreshJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() + 3600_000);

            JwtPayload refreshPayload = new JwtPayload(userId, refreshJti, new Date(), expiration, UserModel.Role.USER);

            request.setCookies(new Cookie(COOKIE_NAME, refreshToken));

            given(jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH)).willReturn(refreshPayload);
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtRegistry).should(never()).revokeAccessToken(any(), any());
            then(jwtRegistry).should().revokeRefreshToken(userId, refreshJti);
        }

        @Test
        @DisplayName("Bearer 접두사가 없는 Authorization 헤더는 무시한다")
        void withNonBearerAuthHeader_skipsAccessTokenRevocation() {
            // given
            request.addHeader("Authorization", "Basic credentials");
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtProvider).should(never()).verifyAndParse(any(), eq(TokenType.ACCESS));
            then(jwtRegistry).should(never()).revokeAccessToken(any(), any());
        }

        @Test
        @DisplayName("리프레시 토큰 쿠키가 없으면 리프레시 토큰 무효화를 건너뛴다")
        void withoutRefreshTokenCookie_skipsRefreshTokenRevocation() {
            // given
            String accessToken = "access-token";
            UUID userId = UUID.randomUUID();
            UUID accessJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() + 3600_000);

            JwtPayload accessPayload = new JwtPayload(userId, accessJti, new Date(), expiration, UserModel.Role.USER);

            request.addHeader("Authorization", "Bearer " + accessToken);

            given(jwtProvider.verifyAndParse(accessToken, TokenType.ACCESS)).willReturn(accessPayload);
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtRegistry).should().revokeAccessToken(accessJti, expiration);
            then(jwtRegistry).should(never()).revokeRefreshToken(any(), any());
        }

        @Test
        @DisplayName("유효하지 않은 액세스 토큰은 무시하고 계속 진행한다")
        void withInvalidAccessToken_continuesLogout() {
            // given
            String invalidAccessToken = "invalid-access-token";
            String refreshToken = "refresh-token";
            UUID userId = UUID.randomUUID();
            UUID refreshJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() + 3600_000);

            JwtPayload refreshPayload = new JwtPayload(userId, refreshJti, new Date(), expiration, UserModel.Role.USER);

            request.addHeader("Authorization", "Bearer " + invalidAccessToken);
            request.setCookies(new Cookie(COOKIE_NAME, refreshToken));

            given(jwtProvider.verifyAndParse(invalidAccessToken, TokenType.ACCESS))
                .willThrow(InvalidTokenException.create());
            given(jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH)).willReturn(refreshPayload);
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtRegistry).should(never()).revokeAccessToken(any(), any());
            then(jwtRegistry).should().revokeRefreshToken(userId, refreshJti);
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰은 무시하고 계속 진행한다")
        void withInvalidRefreshToken_continuesLogout() {
            // given
            String invalidRefreshToken = "invalid-refresh-token";

            request.setCookies(new Cookie(COOKIE_NAME, invalidRefreshToken));

            given(jwtProvider.verifyAndParse(invalidRefreshToken, TokenType.REFRESH))
                .willThrow(InvalidTokenException.create());
            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(new Cookie(COOKIE_NAME, ""));

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            then(jwtRegistry).should(never()).revokeRefreshToken(any(), any());
        }

        @Test
        @DisplayName("로그아웃 시 만료된 리프레시 토큰 쿠키를 설정한다")
        void setsExpiredCookie() {
            // given
            Cookie expiredCookie = new Cookie(COOKIE_NAME, "");
            expiredCookie.setMaxAge(0);

            given(cookieProvider.createExpiredRefreshTokenCookie()).willReturn(expiredCookie);

            // when
            signOutHandler.logout(request, response, authentication);

            // then
            assertThat(response.getCookies()).hasSize(1);
            assertThat(response.getCookies()[0].getName()).isEqualTo(COOKIE_NAME);
            assertThat(response.getCookies()[0].getMaxAge()).isZero();
        }
    }
}
