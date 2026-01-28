package com.mopl.sse.repository;

import com.mopl.sse.config.SseProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class RedisEmitterRepository {

    private static final String EVENT_CACHE_KEY_PREFIX = "sse:events:";

    @Getter
    private final Map<UUID, SseEmitter> localEmitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final SseProperties sseProperties;

    public void save(UUID userId, SseEmitter emitter) {
        localEmitters.put(userId, emitter);
    }

    public Optional<SseEmitter> findByUserId(UUID userId) {
        return Optional.ofNullable(localEmitters.get(userId));
    }

    public void deleteByUserId(UUID userId) {
        localEmitters.remove(userId);
    }

    public boolean existsLocally(UUID userId) {
        return localEmitters.containsKey(userId);
    }

    public void cacheEvent(UUID userId, UUID eventId, Object eventData) {
        String key = EVENT_CACHE_KEY_PREFIX + userId;
        long score = extractTimestampFromUuidV7(eventId);

        redisTemplate.opsForZSet().add(key, new CachedEvent(eventId, eventData), score);
        redisTemplate.expire(key, sseProperties.eventCache().ttl());

        int maxSize = sseProperties.eventCache().maxSize();
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > maxSize) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - maxSize - 1);
        }
    }

    public List<CachedEvent> getEventsAfter(UUID userId, UUID lastEventId) {
        String key = EVENT_CACHE_KEY_PREFIX + userId;
        long lastScore = extractTimestampFromUuidV7(lastEventId);

        Set<TypedTuple<Object>> results = redisTemplate.opsForZSet()
            .rangeByScoreWithScores(key, lastScore + 1, Double.MAX_VALUE);

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
            .map(TypedTuple::getValue)
            .filter(CachedEvent.class::isInstance)
            .map(CachedEvent.class::cast)
            .toList();
    }

    private long extractTimestampFromUuidV7(UUID uuid) {
        return (uuid.getMostSignificantBits() >> 16) & 0xFFFFFFFFFFFFL;
    }

    public record CachedEvent(UUID eventId, Object data) {
    }
}
