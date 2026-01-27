package com.mopl.security.jwt.registry;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.logging.context.LogContext;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtInformation;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class InMemoryJwtRegistry implements JwtRegistry {

    private final int maxSessions;
    private final Map<UUID, LinkedHashMap<UUID, JwtInformation>> whitelist = new HashMap<>();
    private final Map<UUID, Date> blacklist = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryJwtRegistry(JwtProperties jwtProperties) {
        this.maxSessions = jwtProperties.maxSessions();
        LogContext.with("maxSessions", maxSessions).info("InMemoryJwtRegistry initialized");
    }

    @Override
    public void register(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userId();
        UUID jti = jwtInformation.refreshTokenJti();

        writeLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.computeIfAbsent(
                userId, key -> new LinkedHashMap<>(16, 0.75f, false)
            );

            if (sessions.size() >= maxSessions) {
                evictOldestSession(userId, sessions);
            }

            sessions.put(jti, jwtInformation);
        });
    }

    @Override
    public void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userId();

        writeLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(userId);

            if (isInvalidSession(sessions, oldRefreshTokenJti)) {
                LogContext.with("userId", userId).and("jti", oldRefreshTokenJti)
                    .warn("Invalid refresh token rotation attempt - revoking all sessions");
                revokeAllByUserId(userId);
                throw InvalidTokenException.create();
            }

            JwtInformation oldInfo = sessions.remove(oldRefreshTokenJti);
            addToBlacklist(oldInfo);

            sessions.put(newJwtInformation.refreshTokenJti(), newJwtInformation);
        });
    }

    @Override
    public boolean isAccessTokenInBlacklist(UUID accessTokenJti) {
        return readLock(() -> blacklist.containsKey(accessTokenJti));
    }

    @Override
    public boolean isRefreshTokenNotInWhitelist(UUID userId, UUID refreshTokenJti) {
        return readLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(userId);
            return isInvalidSession(sessions, refreshTokenJti);
        });
    }

    @Override
    public void revokeAccessToken(UUID accessTokenJti, Date expiration) {
        writeLock(() -> {
            if (expiration.after(new Date())) {
                blacklist.put(accessTokenJti, expiration);
            }
        });
    }

    @Override
    public void revokeRefreshToken(UUID userId, UUID refreshTokenJti) {
        writeLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(userId);
            if (sessions != null) {
                sessions.remove(refreshTokenJti);
                if (sessions.isEmpty()) {
                    whitelist.remove(userId);
                }
            }
        });
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        writeLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.remove(userId);
            if (sessions != null) {
                sessions.values().forEach(this::addToBlacklist);
            }
        });
    }

    @Override
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpired() {
        try {
            writeLock(() -> {
                Date now = new Date();
                blacklist.entrySet().removeIf(entry -> entry.getValue().before(now));

                whitelist.entrySet().removeIf(entry -> {
                    LinkedHashMap<UUID, JwtInformation> sessions = entry.getValue();
                    sessions.values().removeIf(info -> info
                        .refreshTokenPayload().exp().before(now)
                    );
                    return sessions.isEmpty();
                });
            });
        } catch (Exception e) {
            LogContext.with("error", e.getClass().getSimpleName()).error("Error clearing expired tokens", e);
        }
    }

    private void evictOldestSession(UUID userId, Map<UUID, JwtInformation> sessions) {
        Map.Entry<UUID, JwtInformation> firstEntry = sessions.entrySet().iterator().next();
        addToBlacklist(firstEntry.getValue());
        sessions.remove(firstEntry.getKey());
        LogContext.with("userId", userId).and("jti", firstEntry.getKey()).info("Session evicted due to max session limit");
    }

    private void addToBlacklist(JwtInformation info) {
        if (info.accessTokenPayload().exp().after(new Date())) {
            blacklist.put(info.accessTokenPayload().jti(), info.accessTokenPayload().exp());
        }
    }

    private boolean isInvalidSession(Map<UUID, JwtInformation> sessions, UUID jti) {
        return sessions == null || !sessions.containsKey(jti);
    }

    private void writeLock(Runnable action) {
        lock.writeLock().lock();
        try {
            action.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T readLock(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}
