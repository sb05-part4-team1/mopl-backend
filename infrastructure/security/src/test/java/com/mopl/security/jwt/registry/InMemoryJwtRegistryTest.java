package com.mopl.security.jwt.registry;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InMemoryJwtRegistry 단위 테스트")
class InMemoryJwtRegistryTest {

    private InMemoryJwtRegistry registry;

    private static final int MAX_SESSIONS = 3;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config("access-secret-key-for-testing-32-bytes", Duration.ofMinutes(30), null),
            new JwtProperties.Config("refresh-secret-key-for-testing-32-bytes", Duration.ofDays(7), null),
            MAX_SESSIONS,
            JwtProperties.JwtRegistryType.IN_MEMORY,
            "REFRESH_TOKEN"
        );
        registry = new InMemoryJwtRegistry(jwtProperties);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTest {

        @Test
        @DisplayName("새 세션을 등록한다")
        void registersNewSession() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);

            // when
            registry.register(jwtInfo);

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, jwtInfo.refreshTokenJti())).isFalse();
        }

        @Test
        @DisplayName("최대 세션 수를 초과하면 가장 오래된 세션을 제거한다")
        void whenMaxSessionsExceeded_evictsOldestSession() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation firstSession = createJwtInformation(userId);
            JwtInformation secondSession = createJwtInformation(userId);
            JwtInformation thirdSession = createJwtInformation(userId);
            JwtInformation fourthSession = createJwtInformation(userId);

            // when
            registry.register(firstSession);
            registry.register(secondSession);
            registry.register(thirdSession);
            registry.register(fourthSession);

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, firstSession.refreshTokenJti())).isTrue();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, secondSession.refreshTokenJti())).isFalse();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, thirdSession.refreshTokenJti())).isFalse();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, fourthSession.refreshTokenJti())).isFalse();
        }

        @Test
        @DisplayName("퇴출된 세션의 액세스 토큰은 블랙리스트에 추가된다")
        void evictedSession_accessTokenIsBlacklisted() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation firstSession = createJwtInformation(userId);

            registry.register(firstSession);
            registry.register(createJwtInformation(userId));
            registry.register(createJwtInformation(userId));

            // when
            registry.register(createJwtInformation(userId));

            // then
            assertThat(registry.isAccessTokenInBlacklist(firstSession.accessTokenPayload().jti())).isTrue();
        }
    }

    @Nested
    @DisplayName("rotate()")
    class RotateTest {

        @Test
        @DisplayName("기존 리프레시 토큰을 새 토큰으로 교체한다")
        void rotatesToken() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation oldJwtInfo = createJwtInformation(userId);
            JwtInformation newJwtInfo = createJwtInformation(userId);

            registry.register(oldJwtInfo);

            // when
            registry.rotate(oldJwtInfo.refreshTokenJti(), newJwtInfo);

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, oldJwtInfo.refreshTokenJti())).isTrue();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, newJwtInfo.refreshTokenJti())).isFalse();
        }

        @Test
        @DisplayName("교체된 이전 액세스 토큰은 블랙리스트에 추가된다")
        void rotatedOldAccessToken_isBlacklisted() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation oldJwtInfo = createJwtInformation(userId);
            JwtInformation newJwtInfo = createJwtInformation(userId);

            registry.register(oldJwtInfo);

            // when
            registry.rotate(oldJwtInfo.refreshTokenJti(), newJwtInfo);

            // then
            assertThat(registry.isAccessTokenInBlacklist(oldJwtInfo.accessTokenPayload().jti())).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 교체 시도하면 예외가 발생한다")
        void withInvalidRefreshToken_throwsException() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation validJwtInfo = createJwtInformation(userId);
            JwtInformation newJwtInfo = createJwtInformation(userId);
            UUID invalidJti = UUID.randomUUID();

            registry.register(validJwtInfo);

            // when & then
            assertThatThrownBy(() -> registry.rotate(invalidJti, newJwtInfo))
                .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 교체 시도 시 모든 세션이 무효화된다")
        void withInvalidToken_revokesAllSessions() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation session1 = createJwtInformation(userId);
            JwtInformation session2 = createJwtInformation(userId);
            JwtInformation newJwtInfo = createJwtInformation(userId);
            UUID invalidJti = UUID.randomUUID();

            registry.register(session1);
            registry.register(session2);

            // when
            try {
                registry.rotate(invalidJti, newJwtInfo);
            } catch (InvalidTokenException ignored) {
            }

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, session1.refreshTokenJti())).isTrue();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, session2.refreshTokenJti())).isTrue();
        }
    }

    @Nested
    @DisplayName("isAccessTokenInBlacklist()")
    class IsAccessTokenInBlacklistTest {

        @Test
        @DisplayName("블랙리스트에 있는 토큰은 true를 반환한다")
        void withBlacklistedToken_returnsTrue() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);

            registry.register(jwtInfo);
            registry.revokeAccessToken(jwtInfo.accessTokenPayload().jti(), jwtInfo.accessTokenPayload().exp());

            // when
            boolean result = registry.isAccessTokenInBlacklist(jwtInfo.accessTokenPayload().jti());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("블랙리스트에 없는 토큰은 false를 반환한다")
        void withNonBlacklistedToken_returnsFalse() {
            // given
            UUID randomJti = UUID.randomUUID();

            // when
            boolean result = registry.isAccessTokenInBlacklist(randomJti);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isRefreshTokenNotInWhitelist()")
    class IsRefreshTokenNotInWhitelistTest {

        @Test
        @DisplayName("화이트리스트에 있는 토큰은 false를 반환한다")
        void withWhitelistedToken_returnsFalse() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);

            registry.register(jwtInfo);

            // when
            boolean result = registry.isRefreshTokenNotInWhitelist(userId, jwtInfo.refreshTokenJti());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("화이트리스트에 없는 토큰은 true를 반환한다")
        void withNonWhitelistedToken_returnsTrue() {
            // given
            UUID userId = UUID.randomUUID();
            UUID randomJti = UUID.randomUUID();

            // when
            boolean result = registry.isRefreshTokenNotInWhitelist(userId, randomJti);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("등록되지 않은 사용자의 토큰은 true를 반환한다")
        void withUnregisteredUser_returnsTrue() {
            // given
            UUID unregisteredUserId = UUID.randomUUID();
            UUID randomJti = UUID.randomUUID();

            // when
            boolean result = registry.isRefreshTokenNotInWhitelist(unregisteredUserId, randomJti);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("revokeAccessToken()")
    class RevokeAccessTokenTest {

        @Test
        @DisplayName("만료되지 않은 토큰을 블랙리스트에 추가한다")
        void withNonExpiredToken_addsToBlacklist() {
            // given
            UUID jti = UUID.randomUUID();
            Date futureExpiration = new Date(System.currentTimeMillis() + 60000);

            // when
            registry.revokeAccessToken(jti, futureExpiration);

            // then
            assertThat(registry.isAccessTokenInBlacklist(jti)).isTrue();
        }

        @Test
        @DisplayName("이미 만료된 토큰은 블랙리스트에 추가하지 않는다")
        void withExpiredToken_doesNotAddToBlacklist() {
            // given
            UUID jti = UUID.randomUUID();
            Date pastExpiration = new Date(System.currentTimeMillis() - 60000);

            // when
            registry.revokeAccessToken(jti, pastExpiration);

            // then
            assertThat(registry.isAccessTokenInBlacklist(jti)).isFalse();
        }
    }

    @Nested
    @DisplayName("revokeRefreshToken()")
    class RevokeRefreshTokenTest {

        @Test
        @DisplayName("리프레시 토큰을 화이트리스트에서 제거한다")
        void removesFromWhitelist() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);

            registry.register(jwtInfo);

            // when
            registry.revokeRefreshToken(userId, jwtInfo.refreshTokenJti());

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, jwtInfo.refreshTokenJti())).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 토큰 제거 시도 시 예외가 발생하지 않는다")
        void withNonExistentToken_doesNotThrowException() {
            // given
            UUID userId = UUID.randomUUID();
            UUID randomJti = UUID.randomUUID();

            // when & then
            registry.revokeRefreshToken(userId, randomJti);
        }
    }

    @Nested
    @DisplayName("revokeAllByUserId()")
    class RevokeAllByUserIdTest {

        @Test
        @DisplayName("사용자의 모든 세션을 무효화한다")
        void revokesAllSessions() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation session1 = createJwtInformation(userId);
            JwtInformation session2 = createJwtInformation(userId);

            registry.register(session1);
            registry.register(session2);

            // when
            registry.revokeAllByUserId(userId);

            // then
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, session1.refreshTokenJti())).isTrue();
            assertThat(registry.isRefreshTokenNotInWhitelist(userId, session2.refreshTokenJti())).isTrue();
        }

        @Test
        @DisplayName("모든 세션의 액세스 토큰이 블랙리스트에 추가된다")
        void allAccessTokens_areBlacklisted() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation session1 = createJwtInformation(userId);
            JwtInformation session2 = createJwtInformation(userId);

            registry.register(session1);
            registry.register(session2);

            // when
            registry.revokeAllByUserId(userId);

            // then
            assertThat(registry.isAccessTokenInBlacklist(session1.accessTokenPayload().jti())).isTrue();
            assertThat(registry.isAccessTokenInBlacklist(session2.accessTokenPayload().jti())).isTrue();
        }
    }

    @Nested
    @DisplayName("clearExpired()")
    class ClearExpiredTest {

        @Test
        @DisplayName("만료되지 않은 블랙리스트 항목은 유지된다")
        void retainsNonExpiredBlacklistEntries() {
            // given
            UUID jti = UUID.randomUUID();
            registry.revokeAccessToken(jti, new Date(System.currentTimeMillis() + 60000));

            // when
            registry.clearExpired();

            // then (아직 만료되지 않았으므로 남아있어야 함)
            assertThat(registry.isAccessTokenInBlacklist(jti)).isTrue();
        }
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
        return new JwtInformation("access-token", "refresh-token", accessPayload, refreshPayload);
    }
}
