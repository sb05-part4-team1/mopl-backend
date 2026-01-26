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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RedisJwtRegistry 단위 테스트")
class RedisJwtRegistryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisJwtRegistry redisJwtRegistry;

    private static final int MAX_SESSIONS = 3;
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config("access-secret", Duration.ofMinutes(30), null),
            new JwtProperties.Config("refresh-secret", REFRESH_TOKEN_EXPIRATION, null),
            MAX_SESSIONS,
            JwtProperties.JwtRegistryType.REDIS,
            "REFRESH_TOKEN"
        );

        given(redisTemplate.opsForHash()).willReturn(hashOperations);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        redisJwtRegistry = new RedisJwtRegistry(redisTemplate, jwtProperties);
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
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.size(whitelistKey)).willReturn(0L);

            // when
            redisJwtRegistry.register(jwtInfo);

            // then
            then(hashOperations).should().put(
                eq(whitelistKey),
                eq(jwtInfo.refreshTokenJti().toString()),
                any(RedisJwtRegistry.SessionInfo.class)
            );
            then(redisTemplate).should().expire(whitelistKey, REFRESH_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("최대 세션 초과 시 가장 오래된 세션을 제거한다")
        void evictsOldestSessionWhenMaxExceeded() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.size(whitelistKey)).willReturn((long) MAX_SESSIONS);

            Set<Object> existingKeys = new HashSet<>();
            UUID oldJti = UUID.randomUUID();
            existingKeys.add(oldJti.toString());
            given(hashOperations.keys(whitelistKey)).willReturn(existingKeys);

            RedisJwtRegistry.SessionInfo oldSession = createSessionInfo();
            given(hashOperations.get(whitelistKey, oldJti.toString())).willReturn(oldSession);

            // when
            redisJwtRegistry.register(jwtInfo);

            // then
            then(hashOperations).should().delete(whitelistKey, oldJti.toString());
            then(hashOperations).should().put(
                eq(whitelistKey),
                eq(jwtInfo.refreshTokenJti().toString()),
                any(RedisJwtRegistry.SessionInfo.class)
            );
        }
    }

    @Nested
    @DisplayName("rotate()")
    class RotateTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 로테이션을 수행한다")
        void rotatesWithValidRefreshToken() {
            // given
            UUID userId = UUID.randomUUID();
            UUID oldJti = UUID.randomUUID();
            JwtInformation newJwtInfo = createJwtInformation(userId);
            String whitelistKey = "jwt:whitelist:" + userId;

            RedisJwtRegistry.SessionInfo oldSession = createSessionInfo();
            given(hashOperations.get(whitelistKey, oldJti.toString())).willReturn(oldSession);

            // when
            redisJwtRegistry.rotate(oldJti, newJwtInfo);

            // then
            then(hashOperations).should().delete(whitelistKey, oldJti.toString());
            then(hashOperations).should().put(
                eq(whitelistKey),
                eq(newJwtInfo.refreshTokenJti().toString()),
                any(RedisJwtRegistry.SessionInfo.class)
            );
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 로테이션 시도 시 모든 세션을 무효화하고 예외 발생")
        void throwsExceptionAndRevokesAllWhenInvalidToken() {
            // given
            UUID userId = UUID.randomUUID();
            UUID invalidJti = UUID.randomUUID();
            JwtInformation newJwtInfo = createJwtInformation(userId);
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.get(whitelistKey, invalidJti.toString())).willReturn(null);
            given(hashOperations.entries(whitelistKey)).willReturn(new HashMap<>());

            // when & then
            assertThatThrownBy(() -> redisJwtRegistry.rotate(invalidJti, newJwtInfo))
                .isInstanceOf(InvalidTokenException.class);

            then(redisTemplate).should().delete(whitelistKey);
        }
    }

    @Nested
    @DisplayName("isAccessTokenInBlacklist()")
    class IsAccessTokenInBlacklistTest {

        @Test
        @DisplayName("블랙리스트에 있는 토큰은 true를 반환한다")
        void returnsTrueForBlacklistedToken() {
            // given
            UUID accessTokenJti = UUID.randomUUID();
            String blacklistKey = "jwt:blacklist:" + accessTokenJti;

            given(redisTemplate.hasKey(blacklistKey)).willReturn(true);

            // when
            boolean result = redisJwtRegistry.isAccessTokenInBlacklist(accessTokenJti);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("블랙리스트에 없는 토큰은 false를 반환한다")
        void returnsFalseForNonBlacklistedToken() {
            // given
            UUID accessTokenJti = UUID.randomUUID();
            String blacklistKey = "jwt:blacklist:" + accessTokenJti;

            given(redisTemplate.hasKey(blacklistKey)).willReturn(false);

            // when
            boolean result = redisJwtRegistry.isAccessTokenInBlacklist(accessTokenJti);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isRefreshTokenNotInWhitelist()")
    class IsRefreshTokenNotInWhitelistTest {

        @Test
        @DisplayName("화이트리스트에 있는 토큰은 false를 반환한다")
        void returnsFalseForWhitelistedToken() {
            // given
            UUID userId = UUID.randomUUID();
            UUID refreshTokenJti = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.hasKey(whitelistKey, refreshTokenJti.toString())).willReturn(true);

            // when
            boolean result = redisJwtRegistry.isRefreshTokenNotInWhitelist(userId, refreshTokenJti);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("화이트리스트에 없는 토큰은 true를 반환한다")
        void returnsTrueForNonWhitelistedToken() {
            // given
            UUID userId = UUID.randomUUID();
            UUID refreshTokenJti = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.hasKey(whitelistKey, refreshTokenJti.toString())).willReturn(false);

            // when
            boolean result = redisJwtRegistry.isRefreshTokenNotInWhitelist(userId, refreshTokenJti);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("revokeAccessToken()")
    class RevokeAccessTokenTest {

        @Test
        @DisplayName("만료되지 않은 액세스 토큰을 블랙리스트에 추가한다")
        void addsNonExpiredTokenToBlacklist() {
            // given
            UUID accessTokenJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() + 60000);
            String blacklistKey = "jwt:blacklist:" + accessTokenJti;

            // when
            redisJwtRegistry.revokeAccessToken(accessTokenJti, expiration);

            // then
            then(valueOperations).should().set(
                eq(blacklistKey),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
            );
        }

        @Test
        @DisplayName("이미 만료된 토큰은 블랙리스트에 추가하지 않는다")
        void doesNotAddExpiredTokenToBlacklist() {
            // given
            UUID accessTokenJti = UUID.randomUUID();
            Date expiration = new Date(System.currentTimeMillis() - 60000);

            // when
            redisJwtRegistry.revokeAccessToken(accessTokenJti, expiration);

            // then
            then(valueOperations).should(never()).set(anyString(), any(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("revokeRefreshToken()")
    class RevokeRefreshTokenTest {

        @Test
        @DisplayName("리프레시 토큰을 화이트리스트에서 제거한다")
        void removesRefreshTokenFromWhitelist() {
            // given
            UUID userId = UUID.randomUUID();
            UUID refreshTokenJti = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.size(whitelistKey)).willReturn(1L);

            // when
            redisJwtRegistry.revokeRefreshToken(userId, refreshTokenJti);

            // then
            then(hashOperations).should().delete(whitelistKey, refreshTokenJti.toString());
        }

        @Test
        @DisplayName("마지막 세션 제거 시 화이트리스트 키 자체를 삭제한다")
        void deletesWhitelistKeyWhenLastSessionRemoved() {
            // given
            UUID userId = UUID.randomUUID();
            UUID refreshTokenJti = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.size(whitelistKey)).willReturn(0L);

            // when
            redisJwtRegistry.revokeRefreshToken(userId, refreshTokenJti);

            // then
            then(redisTemplate).should().delete(whitelistKey);
        }
    }

    @Nested
    @DisplayName("revokeAllByUserId()")
    class RevokeAllByUserIdTest {

        @Test
        @DisplayName("사용자의 모든 세션을 무효화한다")
        void revokesAllSessionsForUser() {
            // given
            UUID userId = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            Map<Object, Object> sessions = new HashMap<>();
            RedisJwtRegistry.SessionInfo session1 = createSessionInfo();
            RedisJwtRegistry.SessionInfo session2 = createSessionInfo();
            sessions.put(UUID.randomUUID().toString(), session1);
            sessions.put(UUID.randomUUID().toString(), session2);

            given(hashOperations.entries(whitelistKey)).willReturn(sessions);

            // when
            redisJwtRegistry.revokeAllByUserId(userId);

            // then
            then(redisTemplate).should().delete(whitelistKey);
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

    private RedisJwtRegistry.SessionInfo createSessionInfo() {
        return new RedisJwtRegistry.SessionInfo(
            UUID.randomUUID(),
            new Date(System.currentTimeMillis() + 1800_000),
            java.time.Instant.now()
        );
    }
}
