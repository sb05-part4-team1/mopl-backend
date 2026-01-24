package com.mopl.redis.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
        Object stored = redisTemplate.opsForValue().get(
            WatchingSessionRedisKeys.watcherSessionKey(watcherId)
        );

        if (stored instanceof WatchingSessionModel model) {
            return Optional.of(model);
        }

        return Optional.empty();
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long size = redisTemplate.opsForZSet().zCard(
            WatchingSessionRedisKeys.contentWatchersKey(contentId)
        );
        return size != null ? size : 0L;
    }

    @Override
    public Map<UUID, Long> countByContentIdIn(List<UUID> contentIds) {
        if (contentIds.isEmpty()) {
            return Map.of();
        }

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (UUID contentId : contentIds) {
                byte[] key = WatchingSessionRedisKeys.contentWatchersKey(contentId).getBytes();
                connection.zSetCommands().zCard(key);
            }
            return null;
        });

        Map<UUID, Long> result = new HashMap<>();
        for (int i = 0; i < contentIds.size(); i++) {
            Long count = (Long) results.get(i);
            result.put(contentIds.get(i), count != null ? count : 0L);
        }
        return result;
    }
}
