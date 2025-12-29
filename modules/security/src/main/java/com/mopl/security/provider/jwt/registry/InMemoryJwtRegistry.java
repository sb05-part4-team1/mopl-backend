package com.mopl.security.provider.jwt.registry;

import com.mopl.security.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@ConditionalOnProperty(name = "mopl.jwt.registry-type", havingValue = "in-memory", matchIfMissing = true)
@Slf4j
public class InMemoryJwtRegistry implements JwtRegistry {

    private final int maxSessions;
    private final Map<UUID, LinkedHashMap<UUID, JwtInformation>> whitelist = new ConcurrentHashMap<>();
    private final Map<UUID, Date> blacklist = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryJwtRegistry(JwtProperties jwtProperties) {
        this.maxSessions = jwtProperties.maxSessions();
        log.info("InMemoryJwtRegistry 초기화: maxSessions={}", maxSessions);
    }

    @Override
    public void register(JwtInformation jwtInformation) {
        lock.writeLock().lock();
        try {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.computeIfAbsent(
                jwtInformation.userId(),
                k -> new LinkedHashMap<>()
            );

            if (sessions.size() >= maxSessions) {
                var it = sessions.entrySet().iterator();
                if (it.hasNext()) {
                    JwtInformation evicted = it.next().getValue();
                    blacklist.put(evicted.accessTokenJti(), evicted.accessTokenExpiry());
                    it.remove();
                    log.info("최대 세션 초과로 인한 강제 로그아웃: userId={}, evictedAccessJti={}",
                        jwtInformation.userId(), evicted.accessTokenJti());
                }
            }

            sessions.put(jwtInformation.refreshTokenJti(), jwtInformation);
            log.debug("JWT 등록 완료: {}", jwtInformation);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void rotate(UUID oldRefreshTokenJti, JwtInformation newJwtInformation) {
        lock.writeLock().lock();
        try {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(newJwtInformation.userId());
            if (sessions == null || !sessions.containsKey(oldRefreshTokenJti)) {
                log.warn("JWT 로테이션 실패 - 유효하지 않은 리프레시 토큰: userId={}, jti={}",
                    newJwtInformation.userId(), oldRefreshTokenJti);
                return;
            }

            JwtInformation oldJwtInformation = sessions.remove(oldRefreshTokenJti);
            blacklist.put(oldJwtInformation.accessTokenJti(), oldJwtInformation.accessTokenExpiry());
            sessions.put(newJwtInformation.refreshTokenJti(), newJwtInformation);
            log.debug("JWT 로테이션 완료: userId={}", newJwtInformation.userId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isAccessTokenBlacklisted(UUID accessTokenJti) {
        lock.readLock().lock();
        try {
            return blacklist.containsKey(accessTokenJti);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isRefreshTokenValid(UUID userId, UUID refreshTokenJti) {
        Date now = new Date();
        lock.readLock().lock();
        try {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(userId);
            if (sessions == null) {
                return false;
            }
            JwtInformation jwtInformation = sessions.get(refreshTokenJti);
            return jwtInformation != null && jwtInformation.refreshTokenExpiry().after(now);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void revokeTokenPair(JwtInformation jwtInformation) {
        lock.writeLock().lock();
        try {
            blacklist.put(jwtInformation.accessTokenJti(), jwtInformation.accessTokenExpiry());

            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.get(jwtInformation.userId());
            if (sessions != null) {
                sessions.remove(jwtInformation.refreshTokenJti());
                if (sessions.isEmpty()) {
                    whitelist.remove(jwtInformation.userId());
                }
            }
            log.debug("토큰 페어 무효화 완료: userId={}, accessJti={}, refreshJti={}",
                jwtInformation.userId(), jwtInformation.accessTokenJti(), jwtInformation.refreshTokenJti());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        lock.writeLock().lock();
        try {
            LinkedHashMap<UUID, JwtInformation> sessions = whitelist.remove(userId);
            if (sessions != null) {
                for (JwtInformation jwtInformation : sessions.values()) {
                    blacklist.put(jwtInformation.accessTokenJti(), jwtInformation.accessTokenExpiry());
                }
            }
            log.info("유저의 모든 세션 무효화 완료: userId={}", userId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpired() {
        lock.writeLock().lock();
        try {
            Date now = new Date();

            blacklist.entrySet().removeIf(entry -> entry.getValue().before(now));

            whitelist.values().removeIf(sessions -> {
                sessions.values().removeIf(info -> info.refreshTokenExpiry().before(now));
                return sessions.isEmpty(); // 비어버린 유저 세션 맵도 즉시 제거 대상이 됨
            });

            log.debug("만료된 JWT 정보 정리 완료 (at {})", now);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
