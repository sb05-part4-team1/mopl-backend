package com.mopl.security.jwt.registry;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.security.config.JwtProperties;
import com.mopl.security.jwt.provider.JwtInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Slf4j
public class InMemoryJwtRegistry implements JwtRegistry {

    private final int maxSessions;
    private final Map<UUID, LinkedHashMap<UUID, JwtInformation>> whitelist = new HashMap<>();
    private final Map<UUID, Date> blacklist = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryJwtRegistry(JwtProperties jwtProperties) {
        this.maxSessions = jwtProperties.maxSessions();
        log.info("InMemoryJwtRegistry 초기화: maxSessions={}", maxSessions);
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
            log.debug("JWT 등록 완료: userId={}, jti={}", userId, jti);
        });
    }

    @Override
    public void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userId();

        writeLock(() -> {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(userId);

            if (isInvalidSession(sessions, oldRefreshTokenJti)) {
                log.error("유효하지 않은 리프레시 토큰으로 로테이션 시도됨. " +
                    "해당 유저의 모든 세션을 무효화합니다. userId={}, jti={}", userId, oldRefreshTokenJti);
                revokeAllByUserId(userId);
                throw new InvalidTokenException("유효하지 않은 세션입니다. 다시 로그인해 주세요.");
            }

            JwtInformation oldInfo = sessions.remove(oldRefreshTokenJti);
            addToBlacklist(oldInfo);

            sessions.put(newJwtInformation.refreshTokenJti(), newJwtInformation);
            log.debug("JWT 로테이션 완료: userId={}", userId);
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
                log.debug("만료 데이터 정리 완료");
            });
        } catch (Exception e) {
            log.error("만료 데이터 정리 중 오류 발생", e);
        }
    }

    private void evictOldestSession(UUID userId, LinkedHashMap<UUID, JwtInformation> sessions) {
        var firstEntry = sessions.entrySet().iterator().next();
        addToBlacklist(firstEntry.getValue());
        sessions.remove(firstEntry.getKey());
        log.info("최대 세션 초과 퇴출: userId={}, jti={}", userId, firstEntry.getKey());
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
