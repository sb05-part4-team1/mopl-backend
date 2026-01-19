package com.mopl.websocket.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.support.redis.WatchingSessionRedisKeys; // 네가 만든 위치에 맞게 import 수정

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WatchingSessionRepositoryImpl implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public WatchingSessionModel save(WatchingSessionModel model) {
        // 1) id/createdAt 보장 (BaseModel은 자동 생성이 없음)
        WatchingSessionModel savedModel = ensureIdAndCreatedAt(model);

        UUID contentId = savedModel.getContent().getId();
        UUID watcherId = savedModel.getWatcher().getId();

        // 2) content별 watcherId Set에 저장 (watcherCount 용)
        redisTemplate.opsForSet().add(
                WatchingSessionRedisKeys.watchingCountKey(contentId),
                watcherId.toString()
        );

        // 3) watcher -> current sessionId
        redisTemplate.opsForValue().set(
                WatchingSessionRedisKeys.watcherCurrentKey(watcherId),
                savedModel.getId().toString()
        );

        // 4) sessionId -> WatchingSessionModel(상세)
        redisTemplate.opsForValue().set(
                WatchingSessionRedisKeys.sessionKey(savedModel.getId()),
                savedModel
        );

        // 5) content별 sessions ZSET(createdAt 정렬 + 커서용)
        long score = savedModel.getCreatedAt().toEpochMilli();
        redisTemplate.opsForZSet().add(
                WatchingSessionRedisKeys.contentSessionsZsetKey(contentId),
                savedModel.getId().toString(),
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

        // 1) watcher current에서 sessionId 찾기 (LEAVE 안정화)
        String sessionIdStr = getStringValue(WatchingSessionRedisKeys.watcherCurrentKey(watcherId));
        UUID sessionId = parseUuid(sessionIdStr);

        // contentId는 model에서 우선 가져오되, 없으면 session에서 복원
        UUID contentId = null;
        if (model.getContent() != null) {
            contentId = model.getContent().getId();
        }

        // 2) sessionId가 있으면 sessionKey에서 모델을 꺼내 contentId 복원 가능
        if (sessionId != null) {
            Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(sessionId));
            if (stored instanceof WatchingSessionModel storedModel) {
                if (contentId == null && storedModel.getContent() != null) {
                    contentId = storedModel.getContent().getId();
                }
            }
        }

        // 3) watcherCount Set에서 제거
        if (contentId != null) {
            redisTemplate.opsForSet().remove(
                    WatchingSessionRedisKeys.watchingCountKey(contentId),
                    watcherId.toString()
            );
        }

        // 4) watcher current 제거
        redisTemplate.delete(WatchingSessionRedisKeys.watcherCurrentKey(watcherId));

        // 5) session + zset에서 제거
        if (sessionId != null) {
            redisTemplate.delete(WatchingSessionRedisKeys.sessionKey(sessionId));

            if (contentId != null) {
                redisTemplate.opsForZSet().remove(
                        WatchingSessionRedisKeys.contentSessionsZsetKey(contentId),
                        sessionId.toString()
                );
            }
        }
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long count = redisTemplate.opsForSet().size(WatchingSessionRedisKeys.watchingCountKey(contentId));
        return count != null ? count : 0L;
    }

    @Override
    public Optional<WatchingSessionModel> findCurrentByWatcherId(UUID watcherId) {
        String sessionIdStr = getStringValue(WatchingSessionRedisKeys.watcherCurrentKey(watcherId));
        UUID sessionId = parseUuid(sessionIdStr);

        if (sessionId == null) {
            return Optional.empty();
        }

        Object stored = redisTemplate.opsForValue().get(WatchingSessionRedisKeys.sessionKey(sessionId));
        if (stored instanceof WatchingSessionModel model) {
            return Optional.of(model);
        }

        return Optional.empty();
    }

    // ====== helpers ======

    private WatchingSessionModel ensureIdAndCreatedAt(WatchingSessionModel model) {
        UUID id = model.getId() != null ? model.getId() : UUID.randomUUID();
        Instant createdAt = model.getCreatedAt() != null ? model.getCreatedAt() : Instant.now();

        return WatchingSessionModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(model.getDeletedAt())
                .watcher(model.getWatcher())
                .content(model.getContent())
                .build();
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
