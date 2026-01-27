package com.mopl.security.jwt.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.logging.context.LogContext;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtInformation;
import com.mopl.security.jwt.provider.JwtPayload;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisJwtRegistry implements JwtRegistry {

    private static final String WHITELIST_KEY_PREFIX = "jwt:whitelist:";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";

    private static final String REGISTER_SCRIPT = """
        local whitelistKey = KEYS[1]
        local jti = ARGV[1]
        local sessionInfo = ARGV[2]
        local maxSessions = tonumber(ARGV[3])
        local ttlSeconds = tonumber(ARGV[4])
        
        local currentSize = redis.call('HLEN', whitelistKey)
        local evictedJti = nil
        local evictedSession = nil
        
        if currentSize >= maxSessions then
            local entries = redis.call('HGETALL', whitelistKey)
            local oldestJti = nil
            local oldestTime = nil
        
            for i = 1, #entries, 2 do
                local entryJti = entries[i]
                local entryData = cjson.decode(entries[i + 1])
                local createdAt = entryData.createdAt
        
                if oldestTime == nil or createdAt < oldestTime then
                    oldestTime = createdAt
                    oldestJti = entryJti
                    evictedSession = entries[i + 1]
                end
            end
        
            if oldestJti then
                redis.call('HDEL', whitelistKey, oldestJti)
                evictedJti = oldestJti
            end
        end
        
        redis.call('HSET', whitelistKey, jti, sessionInfo)
        redis.call('EXPIRE', whitelistKey, ttlSeconds)
        
        if evictedJti then
            return cjson.encode({evictedJti = evictedJti, evictedSession = evictedSession})
        end
        return nil
        """;

    private static final String ROTATE_SCRIPT = """
        local whitelistKey = KEYS[1]
        local oldJti = ARGV[1]
        local newJti = ARGV[2]
        local newSessionInfo = ARGV[3]
        local ttlSeconds = tonumber(ARGV[4])
        
        local oldSession = redis.call('HGET', whitelistKey, oldJti)
        if not oldSession then
            return cjson.encode({error = 'INVALID_TOKEN'})
        end
        
        redis.call('HDEL', whitelistKey, oldJti)
        redis.call('HSET', whitelistKey, newJti, newSessionInfo)
        redis.call('EXPIRE', whitelistKey, ttlSeconds)
        
        return cjson.encode({oldSession = oldSession})
        """;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisScript<String> registerScript;
    private final RedisScript<String> rotateScript;
    private final int maxSessions;
    private final Duration refreshTokenExpiration;

    public RedisJwtRegistry(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper,
        JwtProperties jwtProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.maxSessions = jwtProperties.maxSessions();
        this.refreshTokenExpiration = jwtProperties.refreshToken().expiration();
        this.registerScript = RedisScript.of(REGISTER_SCRIPT, String.class);
        this.rotateScript = RedisScript.of(ROTATE_SCRIPT, String.class);
        LogContext.with("maxSessions", maxSessions).info("RedisJwtRegistry initialized");
    }

    @Override
    public void register(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userId();
        UUID jti = jwtInformation.refreshTokenJti();
        String whitelistKey = getWhitelistKey(userId);

        SessionInfo sessionInfo = SessionInfo.from(jwtInformation);
        String sessionJson = toJson(sessionInfo);

        String result = redisTemplate.execute(
            registerScript,
            List.of(whitelistKey),
            jti.toString(),
            sessionJson,
            String.valueOf(maxSessions),
            String.valueOf(refreshTokenExpiration.toSeconds())
        );

        // noinspection ConstantValue
        if (result != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(result);
                String evictedJti = rootNode.has("evictedJti") ? rootNode.get("evictedJti").asText() : null;
                JsonNode evictedSessionNode = rootNode.get("evictedSession");

                if (evictedSessionNode != null && !evictedSessionNode.isNull()) {
                    SessionInfo evictedSession = parseSessionInfoFromNode(evictedSessionNode);
                    if (evictedSession != null) {
                        addToBlacklist(evictedSession.accessTokenJti(), evictedSession.accessTokenExp());
                    }
                }
                if (evictedJti != null) {
                    LogContext.with("userId", userId).and("jti", evictedJti).info("Session evicted due to max session limit");
                }
            } catch (JsonProcessingException e) {
                LogContext.with("result", result).error("Failed to parse Lua script result", e);
            }
        }
    }

    @Override
    public void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userId();
        String whitelistKey = getWhitelistKey(userId);

        SessionInfo newSessionInfo = SessionInfo.from(newJwtInformation);
        String newSessionJson = toJson(newSessionInfo);

        String result = redisTemplate.execute(
            rotateScript,
            List.of(whitelistKey),
            oldRefreshTokenJti.toString(),
            newJwtInformation.refreshTokenJti().toString(),
            newSessionJson,
            String.valueOf(refreshTokenExpiration.toSeconds())
        );

        // noinspection ConstantValue
        if (result == null) {
            handleInvalidToken(userId, oldRefreshTokenJti);
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(result);

            if (rootNode.has("error")) {
                handleInvalidToken(userId, oldRefreshTokenJti);
                return;
            }

            JsonNode oldSessionNode = rootNode.get("oldSession");
            if (oldSessionNode != null && !oldSessionNode.isNull()) {
                SessionInfo oldSession = parseSessionInfoFromNode(oldSessionNode);
                if (oldSession != null) {
                    addToBlacklist(oldSession.accessTokenJti(), oldSession.accessTokenExp());
                }
            }

        } catch (JsonProcessingException e) {
            LogContext.with("result", result).error("Failed to parse Lua script result", e);
            handleInvalidToken(userId, oldRefreshTokenJti);
        }
    }

    private void handleInvalidToken(UUID userId, UUID oldRefreshTokenJti) {
        LogContext.with("userId", userId).and("jti", oldRefreshTokenJti)
            .warn("Invalid refresh token rotation attempt - revoking all sessions");
        revokeAllByUserId(userId);
        throw InvalidTokenException.create();
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

        if (redisTemplate.opsForHash().size(whitelistKey) == 0) {
            redisTemplate.delete(whitelistKey);
        }
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        String whitelistKey = getWhitelistKey(userId);

        Map<Object, Object> sessions = redisTemplate.opsForHash().entries(whitelistKey);
        for (Object value : sessions.values()) {
            SessionInfo sessionInfo = parseSessionInfo((String) value);
            if (sessionInfo != null) {
                addToBlacklist(sessionInfo.accessTokenJti(), sessionInfo.accessTokenExp());
            }
        }

        redisTemplate.delete(whitelistKey);
        LogContext.with("userId", userId).warn("All user sessions revoked");
    }

    @Override
    public void clearExpired() {
        // Redis TTL이 자동으로 만료 처리하므로 별도 정리 불필요
    }

    private void addToBlacklist(UUID accessTokenJti, Date expiration) {
        if (accessTokenJti == null || expiration == null) {
            return;
        }
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

    private String toJson(SessionInfo sessionInfo) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "accessTokenJti", sessionInfo.accessTokenJti().toString(),
                "accessTokenExp", sessionInfo.accessTokenExp().getTime(),
                "createdAt", sessionInfo.createdAt().toEpochMilli()
            ));
        } catch (JsonProcessingException e) {
            LogContext.with("sessionInfo", sessionInfo.toString()).error("Failed to serialize SessionInfo to JSON", e);
            return String.format(
                "{\"accessTokenJti\":\"%s\",\"accessTokenExp\":%d,\"createdAt\":%d}",
                sessionInfo.accessTokenJti(),
                sessionInfo.accessTokenExp().getTime(),
                sessionInfo.createdAt().toEpochMilli()
            );
        }
    }

    private SessionInfo parseSessionInfoFromNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            // Lua cjson.encode가 JSON 문자열을 다시 문자열로 인코딩한 경우 처리
            if (node.isTextual()) {
                return parseSessionInfo(node.asText());
            }
            return new SessionInfo(
                UUID.fromString(node.get("accessTokenJti").asText()),
                new Date(node.get("accessTokenExp").asLong()),
                Instant.ofEpochMilli(node.get("createdAt").asLong())
            );
        } catch (Exception e) {
            LogContext.with("node", String.valueOf(node)).error("Failed to parse SessionInfo from JsonNode", e);
            return null;
        }
    }

    private SessionInfo parseSessionInfo(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return parseSessionInfoFromNode(node);
        } catch (Exception e) {
            LogContext.with("json", json).error("Failed to parse SessionInfo", e);
            return null;
        }
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
