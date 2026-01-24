package com.mopl.websocket.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WatchingSessionRepositoryImpl implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public WatchingSessionModel save(WatchingSessionModel model) {
        WatchingSessionModel savedModel = ensureCreatedAt(model);

        UUID contentId = savedModel.getContent().getId();
        UUID watcherId = savedModel.getWatcher().getId();

        // 1) watcher -> WatchingSessionModel 저장
        redisTemplate.opsForValue().set(
            WatchingSessionRedisKeys.watcherSessionKey(watcherId),
            savedModel
        );

        // 2) content별 watchers ZSet에 저장 (member: watcherId, score: joinedAt)
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

        // 1) 세션에서 contentId 조회
        UUID contentId = null;
        if (model.getContent() != null) {
            contentId = model.getContent().getId();
        }

        // contentId가 없으면 저장된 세션에서 복원
        if (contentId == null) {
            Object stored = redisTemplate.opsForValue().get(sessionKey);
            if (stored instanceof WatchingSessionModel storedModel && storedModel.getContent() != null) {
                contentId = storedModel.getContent().getId();
            }
        }

        // 2) watcher 세션 제거
        redisTemplate.delete(sessionKey);

        // 3) content watchers ZSet에서 제거
        if (contentId != null) {
            redisTemplate.opsForZSet().remove(
                WatchingSessionRedisKeys.contentWatchersKey(contentId),
                watcherId.toString()
            );
        }
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long count = redisTemplate.opsForZSet().zCard(
            WatchingSessionRedisKeys.contentWatchersKey(contentId)
        );
        return count != null ? count : 0L;
    }

    @Override
    public Optional<WatchingSessionModel> findCurrentByWatcherId(UUID watcherId) {
        Object stored = redisTemplate.opsForValue().get(
            WatchingSessionRedisKeys.watcherSessionKey(watcherId)
        );

        if (stored instanceof WatchingSessionModel model) {
            return Optional.of(model);
        }

        return Optional.empty();
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
