package com.mopl.security.jwt.registry;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisJwtRegistry implements JwtRegistry {

    private static final String WHITELIST_KEY_PREFIX = "jwt:whitelist:";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxSessions;
    private final Duration refreshTokenExpiration;

    public RedisJwtRegistry(
        RedisTemplate<String, Object> redisTemplate,
        JwtProperties jwtProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.maxSessions = jwtProperties.maxSessions();
        this.refreshTokenExpiration = jwtProperties.refreshToken().expiration();
        log.info("RedisJwtRegistry 초기화: maxSessions={}", maxSessions);
    }

    @Override
    public void register(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userId();
        UUID jti = jwtInformation.refreshTokenJti();
        String whitelistKey = getWhitelistKey(userId);

        Long currentSize = redisTemplate.opsForHash().size(whitelistKey);
        if (currentSize >= maxSessions) {
            evictOldestSession(userId, whitelistKey);
        }

        SessionInfo sessionInfo = SessionInfo.from(jwtInformation);
        redisTemplate.opsForHash().put(whitelistKey, jti.toString(), sessionInfo);
        redisTemplate.expire(whitelistKey, refreshTokenExpiration);

        log.debug("JWT 등록 완료: userId={}, jti={}", userId, jti);
    }

    @Override
    public void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userId();
        String whitelistKey = getWhitelistKey(userId);

        Object existing = redisTemplate.opsForHash().get(whitelistKey, oldRefreshTokenJti.toString());
        if (existing == null) {
            log.error("유효하지 않은 리프레시 토큰으로 로테이션 시도됨. " +
                "해당 유저의 모든 세션을 무효화합니다. userId={}, jti={}", userId, oldRefreshTokenJti);
            revokeAllByUserId(userId);
            throw InvalidTokenException.create();
        }

        SessionInfo oldSession = convertToSessionInfo(existing);
        redisTemplate.opsForHash().delete(whitelistKey, oldRefreshTokenJti.toString());
        addToBlacklist(oldSession.accessTokenJti(), oldSession.accessTokenExp());

        SessionInfo newSessionInfo = SessionInfo.from(newJwtInformation);
        redisTemplate.opsForHash().put(
            whitelistKey,
            newJwtInformation.refreshTokenJti().toString(),
            newSessionInfo
        );
        redisTemplate.expire(whitelistKey, refreshTokenExpiration);

        log.debug("JWT 로테이션 완료: userId={}", userId);
    }

    @Override
    public boolean isAccessTokenInBlacklist(UUID accessTokenJti) {
        String blacklistKey = getBlacklistKey(accessTokenJti);
        return redisTemplate.hasKey(blacklistKey);
    }

    @Override
    public boolean isRefreshTokenNotInWhitelist(UUID userId, UUID refreshTokenJti) {
        String whitelistKey = getWhitelistKey(userId);
        return !redisTemplate.opsForHash().hasKey(whitelistKey, refreshTokenJti.toString());
    }

    @Override
    public void revokeAccessToken(UUID accessTokenJti, Date expiration) {
        if (expiration.after(new Date())) {
            String blacklistKey = getBlacklistKey(accessTokenJti);
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(blacklistKey, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void revokeRefreshToken(UUID userId, UUID refreshTokenJti) {
        String whitelistKey = getWhitelistKey(userId);
        redisTemplate.opsForHash().delete(whitelistKey, refreshTokenJti.toString());

        Long remainingSize = redisTemplate.opsForHash().size(whitelistKey);
        if (remainingSize == 0) {
            redisTemplate.delete(whitelistKey);
        }
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        String whitelistKey = getWhitelistKey(userId);

        Map<Object, Object> sessions = redisTemplate.opsForHash().entries(whitelistKey);
        for (Object value : sessions.values()) {
            SessionInfo sessionInfo = convertToSessionInfo(value);
            addToBlacklist(sessionInfo.accessTokenJti(), sessionInfo.accessTokenExp());
        }

        redisTemplate.delete(whitelistKey);
    }

    @Override
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpired() {
        // Redis TTL이 자동으로 만료 처리하므로 별도 정리 불필요
        log.debug("Redis TTL에 의해 만료 데이터가 자동 정리됩니다.");
    }

    private void evictOldestSession(UUID userId, String whitelistKey) {
        Set<Object> keys = redisTemplate.opsForHash().keys(whitelistKey);
        if (keys.isEmpty()) {
            return;
        }

        String oldestJti = null;
        Instant oldestTime = Instant.MAX;

        for (Object key : keys) {
            Object value = redisTemplate.opsForHash().get(whitelistKey, key);
            SessionInfo sessionInfo = convertToSessionInfo(value);
            if (sessionInfo != null && sessionInfo.createdAt().isBefore(oldestTime)) {
                oldestTime = sessionInfo.createdAt();
                oldestJti = (String) key;
            }
        }

        if (oldestJti != null) {
            Object oldValue = redisTemplate.opsForHash().get(whitelistKey, oldestJti);
            SessionInfo oldSession = convertToSessionInfo(oldValue);
            if (oldSession != null) {
                addToBlacklist(oldSession.accessTokenJti(), oldSession.accessTokenExp());
            }
            redisTemplate.opsForHash().delete(whitelistKey, oldestJti);
            log.info("최대 세션 초과 퇴출: userId={}, jti={}", userId, oldestJti);
        }
    }

    private void addToBlacklist(UUID accessTokenJti, Date expiration) {
        if (expiration.after(new Date())) {
            String blacklistKey = getBlacklistKey(accessTokenJti);
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            redisTemplate.opsForValue().set(blacklistKey, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    private String getWhitelistKey(UUID userId) {
        return WHITELIST_KEY_PREFIX + userId;
    }

    private String getBlacklistKey(UUID accessTokenJti) {
        return BLACKLIST_KEY_PREFIX + accessTokenJti;
    }

    private SessionInfo convertToSessionInfo(Object value) {
        if (value instanceof SessionInfo sessionInfo) {
            return sessionInfo;
        }
        if (value instanceof Map<?, ?> map) {
            return new SessionInfo(
                UUID.fromString((String) map.get("accessTokenJti")),
                new Date((Long) map.get("accessTokenExp")),
                Instant.parse((String) map.get("createdAt"))
            );
        }
        return null;
    }

    public record SessionInfo(
        UUID accessTokenJti,
        Date accessTokenExp,
        Instant createdAt
    ) {
        public static SessionInfo from(JwtInformation jwtInformation) {
            JwtPayload accessPayload = jwtInformation.accessTokenPayload();
            return new SessionInfo(
                accessPayload.jti(),
                accessPayload.exp(),
                Instant.now()
            );
        }
    }
}
