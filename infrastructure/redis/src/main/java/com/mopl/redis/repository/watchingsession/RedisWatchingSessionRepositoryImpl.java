package com.mopl.redis.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
    public WatchingSessionModel save(WatchingSessionModel model) {
        WatchingSessionModel savedModel = ensureCreatedAt(model);

        UUID contentId = savedModel.getContent().getId();
        UUID watcherId = savedModel.getWatcher().getId();

        redisTemplate.opsForValue().set(
            WatchingSessionRedisKeys.watcherSessionKey(watcherId),
            savedModel
        );

        long score = savedModel.getCreatedAt().toEpochMilli();
        redisTemplate.opsForZSet().add(
            WatchingSessionRedisKeys.contentWatchersKey(contentId),
            watcherId.toString(),
            score
        );

        return savedModel;
    }

    @Override
    public void delete(WatchingSessionModel model) {
        if (model == null || model.getWatcher() == null || model.getWatcher().getId() == null) {
            return;
        }

        UUID watcherId = model.getWatcher().getId();
        String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);

        UUID contentId = null;
        if (model.getContent() != null) {
            contentId = model.getContent().getId();
        }

        if (contentId == null) {
            Object stored = redisTemplate.opsForValue().get(sessionKey);
            if (stored instanceof WatchingSessionModel storedModel && storedModel.getContent() != null) {
                contentId = storedModel.getContent().getId();
            }
        }

        redisTemplate.delete(sessionKey);

        if (contentId != null) {
            redisTemplate.opsForZSet().remove(
                WatchingSessionRedisKeys.contentWatchersKey(contentId),
                watcherId.toString()
            );
        }
    }

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

    private WatchingSessionModel ensureCreatedAt(WatchingSessionModel model) {
        if (model.getCreatedAt() != null) {
            return model;
        }

        return WatchingSessionModel.builder()
            .id(model.getId())
            .createdAt(Instant.now())
            .deletedAt(model.getDeletedAt())
            .watcher(model.getWatcher())
            .content(model.getContent())
            .build();
    }
}
