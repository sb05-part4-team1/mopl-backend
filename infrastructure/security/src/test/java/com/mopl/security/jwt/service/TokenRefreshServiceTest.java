package com.mopl.security.jwt.service;

import com.mopl.domain.exception.auth.AccountLockedException;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.jwt.provider.JwtCookieProvider;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.jwt.registry.JwtRegistry;
import com.mopl.security.jwt.service.TokenRefreshService.TokenRefreshResult;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRefreshService 단위 테스트")
class TokenRefreshServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtCookieProvider cookieProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private UserService userService;

    private TokenRefreshService tokenRefreshService;

    @BeforeEach
    void setUp() {
        tokenRefreshService = new TokenRefreshService(
            jwtProvider,
            cookieProvider,
            jwtRegistry,
            userService
        );
    }

    @Nested
    @DisplayName("refresh()")
    class RefreshTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새 토큰 쌍을 발급한다")
        void withValidRefreshToken_issuesNewTokenPair() {
            // given
            String oldRefreshToken = "old-refresh-token";
            UUID userId = UUID.randomUUID();
            UUID oldJti = UUID.randomUUID();
            JwtPayload oldPayload = createPayload(userId, oldJti);

            UserModel user = createUser(userId, false);
            JwtInformation newJwtInfo = createJwtInformation(userId);
            Cookie newCookie = new Cookie("REFRESH_TOKEN", "new-refresh-token");

            given(jwtProvider.verifyAndParse(oldRefreshToken, TokenType.REFRESH)).willReturn(oldPayload);
            given(jwtRegistry.isRefreshTokenNotInWhitelist(userId, oldJti)).willReturn(false);
            given(userService.getById(userId)).willReturn(user);
            given(jwtProvider.issueTokenPair(userId, UserModel.Role.USER)).willReturn(newJwtInfo);
            given(cookieProvider.createRefreshTokenCookie(newJwtInfo.refreshToken())).willReturn(newCookie);

            // when
            TokenRefreshResult result = tokenRefreshService.refresh(oldRefreshToken);

            // then
            assertThat(result.jwtResponse().accessToken()).isEqualTo(newJwtInfo.accessToken());
            assertThat(result.refreshTokenCookie()).isEqualTo(newCookie);
            then(jwtRegistry).should().rotate(oldJti, newJwtInfo);
        }

        @Test
        @DisplayName("화이트리스트에 없는 리프레시 토큰은 예외를 발생시킨다")
        void withTokenNotInWhitelist_throwsException() {
            // given
            String refreshToken = "invalid-refresh-token";
            UUID userId = UUID.randomUUID();
            UUID jti = UUID.randomUUID();
            JwtPayload payload = createPayload(userId, jti);

            given(jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH)).willReturn(payload);
            given(jwtRegistry.isRefreshTokenNotInWhitelist(userId, jti)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> tokenRefreshService.refresh(refreshToken))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("잠긴 계정은 예외를 발생시킨다")
        void withLockedAccount_throwsException() {
            // given
            String refreshToken = "refresh-token";
            UUID userId = UUID.randomUUID();
            UUID jti = UUID.randomUUID();
            JwtPayload payload = createPayload(userId, jti);

            UserModel lockedUser = createUser(userId, true);

            given(jwtProvider.verifyAndParse(refreshToken, TokenType.REFRESH)).willReturn(payload);
            given(jwtRegistry.isRefreshTokenNotInWhitelist(userId, jti)).willReturn(false);
            given(userService.getById(userId)).willReturn(lockedUser);

            // when & then
            assertThatThrownBy(() -> tokenRefreshService.refresh(refreshToken))
                .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("유효하지 않은 토큰 형식은 예외를 발생시킨다")
        void withInvalidTokenFormat_throwsException() {
            // given
            String invalidToken = "invalid-token";
            given(jwtProvider.verifyAndParse(invalidToken, TokenType.REFRESH))
                .willThrow(InvalidTokenException.create());

            // when & then
            assertThatThrownBy(() -> tokenRefreshService.refresh(invalidToken))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("ADMIN 역할 사용자의 토큰도 갱신할 수 있다")
        void withAdminRole_refreshesToken() {
            // given
            String oldRefreshToken = "admin-refresh-token";
            UUID userId = UUID.randomUUID();
            UUID oldJti = UUID.randomUUID();
            JwtPayload oldPayload = new JwtPayload(
                userId,
                oldJti,
                new Date(),
                new Date(System.currentTimeMillis() + 604800_000),
                UserModel.Role.ADMIN
            );

            UserModel adminUser = UserModel.builder()
                .id(userId)
                .email("admin@example.com")
                .name("Admin User")
                .role(UserModel.Role.ADMIN)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            JwtInformation newJwtInfo = createJwtInformation(userId);
            Cookie newCookie = new Cookie("REFRESH_TOKEN", "new-refresh-token");

            given(jwtProvider.verifyAndParse(oldRefreshToken, TokenType.REFRESH)).willReturn(oldPayload);
            given(jwtRegistry.isRefreshTokenNotInWhitelist(userId, oldJti)).willReturn(false);
            given(userService.getById(userId)).willReturn(adminUser);
            given(jwtProvider.issueTokenPair(userId, UserModel.Role.ADMIN)).willReturn(newJwtInfo);
            given(cookieProvider.createRefreshTokenCookie(newJwtInfo.refreshToken())).willReturn(newCookie);

            // when
            TokenRefreshResult result = tokenRefreshService.refresh(oldRefreshToken);

            // then
            assertThat(result.jwtResponse()).isNotNull();
            then(jwtProvider).should().issueTokenPair(userId, UserModel.Role.ADMIN);
        }
    }

    private JwtPayload createPayload(UUID userId, UUID jti) {
        return new JwtPayload(
            userId,
            jti,
            new Date(),
            new Date(System.currentTimeMillis() + 604800_000),
            UserModel.Role.USER
        );
    }

    private UserModel createUser(UUID userId, boolean locked) {
        return UserModel.builder()
            .id(userId)
            .email("test@example.com")
            .name("Test User")
            .role(UserModel.Role.USER)
            .authProvider(UserModel.AuthProvider.EMAIL)
            .createdAt(Instant.now())
            .locked(locked)
            .build();
    }

    private JwtInformation createJwtInformation(UUID userId) {
        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + 1800_000);
        Date refreshExpiry = new Date(now.getTime() + 604800_000);

        JwtPayload accessPayload = new JwtPayload(
            userId,
            UUID.randomUUID(),
            now,
            accessExpiry,
            UserModel.Role.USER
        );
        JwtPayload refreshPayload = new JwtPayload(
            userId,
            UUID.randomUUID(),
            now,
            refreshExpiry,
            UserModel.Role.USER
        );
        return new JwtInformation("new-access-token", "new-refresh-token", accessPayload, refreshPayload);
    }
}
