package com.mopl.jpa.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisWatchingSessionRepository implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_KEY_PREFIX = "ws:session:";        // ws:session:{sessionId}
    private static final String WATCHER_CURRENT_PREFIX = "ws:watcher:";    // ws:watcher:{watcherId}:current
    private static final String WATCHER_CURRENT_SUFFIX = ":current";

    @Override
    public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {
        String sessionIdStr = getStringValue(watcherCurrentKey(watcherId));
        UUID sessionId = parseUuid(sessionIdStr);

        if (sessionId == null) {
            return Optional.empty();
        }

        Object stored = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (stored instanceof WatchingSessionModel model) {
            return Optional.of(model);
        }

        return Optional.empty();
    }

    private String watcherCurrentKey(UUID watcherId) {
        return WATCHER_CURRENT_PREFIX + watcherId + WATCHER_CURRENT_SUFFIX;
    }

    private String sessionKey(UUID sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
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
}
