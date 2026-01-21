package com.mopl.sse.repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class RedisEmitterRepository {

    private static final String CONNECTION_KEY_PREFIX = "sse:conn:";
    private static final Duration CONNECTION_TTL = Duration.ofHours(1);

    private final Map<UUID, SseEmitter> localEmitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    @Getter
    private final String serverId;

    public RedisEmitterRepository(
        RedisTemplate<String, Object> redisTemplate,
        @Value("${sse.server-id:#{T(java.util.UUID).randomUUID().toString()}}") String serverId
    ) {
        this.redisTemplate = redisTemplate;
        this.serverId = serverId;
        log.info("SSE Server ID: {}", serverId);
    }

    public void save(UUID userId, SseEmitter emitter) {
        localEmitters.put(userId, emitter);
        redisTemplate.opsForValue().set(
            CONNECTION_KEY_PREFIX + userId,
            serverId,
            CONNECTION_TTL
        );
        log.debug("Saved emitter for user: {}", userId);
    }

    public Optional<SseEmitter> findByUserId(UUID userId) {
        return Optional.ofNullable(localEmitters.get(userId));
    }

    public void deleteByUserId(UUID userId) {
        localEmitters.remove(userId);
        redisTemplate.delete(CONNECTION_KEY_PREFIX + userId);
        log.debug("Deleted emitter for user: {}", userId);
    }

    public boolean existsLocally(UUID userId) {
        return localEmitters.containsKey(userId);
    }

    public Optional<String> findServerIdByUserId(UUID userId) {
        Object value = redisTemplate.opsForValue().get(CONNECTION_KEY_PREFIX + userId);
        return Optional.ofNullable(value).map(Object::toString);
    }

    public boolean isLocalConnection(UUID userId) {
        return findServerIdByUserId(userId)
            .map(id -> id.equals(serverId))
            .orElse(false);
    }

    public void refreshTtl(UUID userId) {
        String key = CONNECTION_KEY_PREFIX + userId;
        redisTemplate.expire(key, CONNECTION_TTL);
    }
}
