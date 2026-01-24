package com.mopl.redis.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisWatchingSessionRepositoryImpl implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {
        String sessionIdStr = getStringValue(WatchingSessionRedisKeys.watcherCurrentKey(watcherId));
        UUID sessionId = parseUuid(sessionIdStr);

        if (sessionId == null) {
            return Optional.empty();
        }

        Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(
            sessionId));
        if (stored instanceof WatchingSessionModel model) {
            return Optional.of(model);
        }

        return Optional.empty();
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String getStringValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long size = redisTemplate.opsForZSet().zCard(WatchingSessionRedisKeys.contentSessionsZsetKey(contentId));
        return size != null ? size : 0L;
    }

    @Override
    public Map<UUID, Long> countByContentIdIn(List<UUID> contentIds) {
        Map<UUID, Long> result = new HashMap<>();
        for (UUID contentId : contentIds) {
            result.put(contentId, countByContentId(contentId));
        }
        return result;
    }
}
