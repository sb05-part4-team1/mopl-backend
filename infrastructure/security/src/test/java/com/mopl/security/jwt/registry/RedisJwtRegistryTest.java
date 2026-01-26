package com.mopl.security.jwt.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisJwtRegistry redisJwtRegistry;
    private ObjectMapper objectMapper;

    private static final int MAX_SESSIONS = 3;
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        JwtProperties jwtProperties = new JwtProperties(
            new JwtProperties.Config("access-secret", Duration.ofMinutes(30), null),
            new JwtProperties.Config("refresh-secret", REFRESH_TOKEN_EXPIRATION, null),
            MAX_SESSIONS,
            JwtProperties.JwtRegistryType.REDIS,
            "REFRESH_TOKEN"
        );

        given(redisTemplate.opsForHash()).willReturn(hashOperations);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        redisJwtRegistry = new RedisJwtRegistry(redisTemplate, objectMapper, jwtProperties);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTest {

        @Test
        @DisplayName("Lua 스크립트를 사용하여 새 세션을 원자적으로 등록한다")
        @SuppressWarnings("unchecked")
        void registersNewSessionAtomically() {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);

            given(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                anyString(), anyString(), anyString(), anyString()
            )).willReturn(null);

            // when
            redisJwtRegistry.register(jwtInfo);

            // then
            ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
            then(redisTemplate).should().execute(
                any(RedisScript.class),
                keysCaptor.capture(),
                eq(jwtInfo.refreshTokenJti().toString()),
                anyString(),
                eq(String.valueOf(MAX_SESSIONS)),
                eq(String.valueOf(REFRESH_TOKEN_EXPIRATION.toSeconds()))
            );

            assertThat(keysCaptor.getValue()).containsExactly("jwt:whitelist:" + userId);
        }

        @Test
        @DisplayName("세션 퇴출 시 퇴출된 세션의 액세스 토큰을 블랙리스트에 추가한다")
        @SuppressWarnings("unchecked")
        void addsEvictedSessionToBlacklistWhenMaxExceeded() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            JwtInformation jwtInfo = createJwtInformation(userId);
            UUID evictedAccessTokenJti = UUID.randomUUID();
            long futureExpTime = System.currentTimeMillis() + 60000;

            // Lua cjson.encode 결과 시뮬레이션
            // evictedSession은 Redis에서 가져온 JSON 문자열이므로, 결과에서도 문자열로 반환됨
            Map<String, Object> evictedSessionMap = Map.of(
                "accessTokenJti", evictedAccessTokenJti.toString(),
                "accessTokenExp", futureExpTime,
                "createdAt", 1704067200000L  // 2024-01-01T00:00:00Z
            );
            String evictedSessionJson = objectMapper.writeValueAsString(evictedSessionMap);

            Map<String, Object> luaResultMap = Map.of(
                "evictedJti", "old-jti",
                "evictedSession", evictedSessionJson  // 문자열로 저장 (Lua cjson 동작)
            );
            String luaResult = objectMapper.writeValueAsString(luaResultMap);

            given(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                anyString(), anyString(), anyString(), anyString()
            )).willReturn(luaResult);

            // when
            redisJwtRegistry.register(jwtInfo);

            // then
            then(valueOperations).should().set(
                eq("jwt:blacklist:" + evictedAccessTokenJti),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
            );
        }
    }

    @Nested
    @DisplayName("rotate()")
    class RotateTest {

        @Test
        @DisplayName("Lua 스크립트를 사용하여 토큰을 원자적으로 로테이션한다")
        @SuppressWarnings("unchecked")
        void rotatesTokenAtomically() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID oldJti = UUID.randomUUID();
            JwtInformation newJwtInfo = createJwtInformation(userId);
            UUID oldAccessTokenJti = UUID.randomUUID();
            long futureExpTime = System.currentTimeMillis() + 60000;

            // Lua cjson.encode 결과 시뮬레이션
            // 실제 Lua에서는 oldSession이 이미 JSON 문자열이므로 cjson.encode 결과도 문자열로 래핑됨
            Map<String, Object> oldSessionMap = new HashMap<>();
            oldSessionMap.put("accessTokenJti", oldAccessTokenJti.toString());
            oldSessionMap.put("accessTokenExp", futureExpTime);
            oldSessionMap.put("createdAt", 1704067200000L);  // 2024-01-01T00:00:00Z
            String oldSessionJson = objectMapper.writeValueAsString(oldSessionMap);

            Map<String, Object> luaResultMap = new HashMap<>();
            luaResultMap.put("oldSession", oldSessionJson);  // 문자열로 저장 (Lua cjson 동작)
            String luaResult = objectMapper.writeValueAsString(luaResultMap);

            System.out.println("Test luaResult: " + luaResult);

            given(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                anyString(), anyString(), anyString(), anyString()
            )).willReturn(luaResult);

            // when
            redisJwtRegistry.rotate(oldJti, newJwtInfo);

            // then
            then(valueOperations).should().set(
                eq("jwt:blacklist:" + oldAccessTokenJti),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
            );
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 로테이션 시 모든 세션을 무효화하고 예외 발생")
        @SuppressWarnings("unchecked")
        void throwsExceptionAndRevokesAllWhenInvalidToken() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID invalidJti = UUID.randomUUID();
            JwtInformation newJwtInfo = createJwtInformation(userId);

            Map<String, Object> errorResult = Map.of("error", "INVALID_TOKEN");
            String luaResult = objectMapper.writeValueAsString(errorResult);

            given(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                anyString(), anyString(), anyString(), anyString()
            )).willReturn(luaResult);
            given(hashOperations.entries(anyString())).willReturn(new HashMap<>());

            // when & then
            assertThatThrownBy(() -> redisJwtRegistry.rotate(invalidJti, newJwtInfo))
                .isInstanceOf(InvalidTokenException.class);

            then(redisTemplate).should().delete("jwt:whitelist:" + userId);
        }

        @Test
        @DisplayName("Lua 스크립트 결과가 null이면 모든 세션을 무효화하고 예외 발생")
        @SuppressWarnings("unchecked")
        void throwsExceptionWhenLuaResultIsNull() {
            // given
            UUID userId = UUID.randomUUID();
            UUID invalidJti = UUID.randomUUID();
            JwtInformation newJwtInfo = createJwtInformation(userId);

            given(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                anyString(), anyString(), anyString(), anyString()
            )).willReturn(null);
            given(hashOperations.entries(anyString())).willReturn(new HashMap<>());

            // when & then
            assertThatThrownBy(() -> redisJwtRegistry.rotate(invalidJti, newJwtInfo))
                .isInstanceOf(InvalidTokenException.class);
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
        @DisplayName("사용자의 모든 세션을 무효화하고 액세스 토큰을 블랙리스트에 추가한다")
        void revokesAllSessionsAndBlacklistsAccessTokens() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            UUID accessTokenJti1 = UUID.randomUUID();
            UUID accessTokenJti2 = UUID.randomUUID();
            long futureExpTime = System.currentTimeMillis() + 60000;

            String session1Json = objectMapper.writeValueAsString(Map.of(
                "accessTokenJti", accessTokenJti1.toString(),
                "accessTokenExp", futureExpTime,
                "createdAt", 1704067200000L
            ));
            String session2Json = objectMapper.writeValueAsString(Map.of(
                "accessTokenJti", accessTokenJti2.toString(),
                "accessTokenExp", futureExpTime,
                "createdAt", 1704070800000L
            ));

            Map<Object, Object> sessions = new HashMap<>();
            sessions.put("jti1", session1Json);
            sessions.put("jti2", session2Json);

            given(hashOperations.entries(whitelistKey)).willReturn(sessions);

            // when
            redisJwtRegistry.revokeAllByUserId(userId);

            // then
            then(redisTemplate).should().delete(whitelistKey);
            then(valueOperations).should().set(
                eq("jwt:blacklist:" + accessTokenJti1),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
            );
            then(valueOperations).should().set(
                eq("jwt:blacklist:" + accessTokenJti2),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
            );
        }

        @Test
        @DisplayName("세션이 없어도 화이트리스트 키를 삭제한다")
        void deletesWhitelistKeyEvenWhenNoSessions() {
            // given
            UUID userId = UUID.randomUUID();
            String whitelistKey = "jwt:whitelist:" + userId;

            given(hashOperations.entries(whitelistKey)).willReturn(new HashMap<>());

            // when
            redisJwtRegistry.revokeAllByUserId(userId);

            // then
            then(redisTemplate).should().delete(whitelistKey);
        }
    }

    @Nested
    @DisplayName("clearExpired()")
    class ClearExpiredTest {

        @Test
        @DisplayName("Redis TTL이 자동 처리하므로 아무 작업도 하지 않는다")
        void doesNothingAsRedisTtlHandlesExpiration() {
            // when
            redisJwtRegistry.clearExpired();

            // then
            then(redisTemplate).shouldHaveNoMoreInteractions();
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
