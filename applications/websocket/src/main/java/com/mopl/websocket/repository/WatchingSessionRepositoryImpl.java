package com.mopl.websocket.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WatchingSessionRepositoryImpl implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // ====== 기존 watcherCount(Set) 유지 ======
    private static final String COUNT_KEY_PREFIX = "watching_count:";

    // ====== 추가: 조회/세션 저장용 ======
    private static final String SESSION_KEY_PREFIX = "ws:session:";                 // ws:session:{sessionId}
    private static final String CONTENT_SESSIONS_ZSET_PREFIX = "ws:content:";       // ws:content:{contentId}:sessions
    private static final String CONTENT_SESSIONS_ZSET_SUFFIX = ":sessions";
    private static final String WATCHER_CURRENT_PREFIX = "ws:watcher:";             // ws:watcher:{watcherId}:current
    private static final String WATCHER_CURRENT_SUFFIX = ":current";

    @Override
    public WatchingSessionModel save(WatchingSessionModel model) {
        // 1) id/createdAt 보장 (BaseModel은 자동 생성이 없음)
        WatchingSessionModel savedModel = ensureIdAndCreatedAt(model);

        UUID contentId = savedModel.getContent().getId();
        UUID watcherId = savedModel.getWatcher().getId();

        // 2) 기존 로직 유지: content별 watcherId Set에 저장 (watcherCount 용)
        redisTemplate.opsForSet().add(
            COUNT_KEY_PREFIX + contentId,
            watcherId.toString()
        );

        // 3) 추가 저장: watcher -> current sessionId
        redisTemplate.opsForValue().set(
            watcherCurrentKey(watcherId),
            savedModel.getId().toString()
        );

        // 4) 추가 저장: sessionId -> WatchingSessionModel(상세)
        redisTemplate.opsForValue().set(
            sessionKey(savedModel.getId()),
            savedModel
        );

        // 5) 추가 저장: content별 sessions ZSET(createdAt 정렬 + 커서용)
        long score = savedModel.getCreatedAt().toEpochMilli();
        redisTemplate.opsForZSet().add(
            contentSessionsZsetKey(contentId),
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
        String sessionIdStr = getStringValue(watcherCurrentKey(watcherId));
        UUID sessionId = parseUuid(sessionIdStr);

        // contentId는 model에서 우선 가져오되, 없으면 session에서 복원
        UUID contentId = null;
        if (model.getContent() != null) {
            contentId = model.getContent().getId();
        }

        // 2) sessionId가 있으면 sessionKey에서 모델을 꺼내 contentId 복원 가능
        if (sessionId != null) {
            Object stored = redisTemplate.opsForValue().get(sessionKey(sessionId));
            if (stored instanceof WatchingSessionModel storedModel) {
                if (contentId == null && storedModel.getContent() != null) {
                    contentId = storedModel.getContent().getId();
                }
            }
        }

        // 3) 기존 로직 유지: watcherCount Set에서 제거
        if (contentId != null) {
            redisTemplate.opsForSet().remove(
                COUNT_KEY_PREFIX + contentId,
                watcherId.toString()
            );
        }

        // 4) 추가 삭제: watcher current 제거
        redisTemplate.delete(watcherCurrentKey(watcherId));

        // 5) 추가 삭제: session + zset에서 제거
        if (sessionId != null) {
            redisTemplate.delete(sessionKey(sessionId));

            if (contentId != null) {
                redisTemplate.opsForZSet().remove(
                    contentSessionsZsetKey(contentId),
                    sessionId.toString()
                );
            }
        }
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long count = redisTemplate.opsForSet().size(COUNT_KEY_PREFIX + contentId);
        return count != null ? count : 0L;
    }

    @Override
    public Optional<WatchingSessionModel> findCurrentByWatcherId(UUID watcherId) {
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


    // ====== helpers ======

    private WatchingSessionModel ensureIdAndCreatedAt(WatchingSessionModel model) {
        UUID id = model.getId() != null ? model.getId() : UUID.randomUUID();
        Instant createdAt = model.getCreatedAt() != null ? model.getCreatedAt() : Instant.now();

        // WatchingSessionModel은 @SuperBuilder라 BaseModel 필드(id/createdAt)도 세팅 가능
        return WatchingSessionModel.builder()
            .id(id)
            .createdAt(createdAt)
            .deletedAt(model.getDeletedAt())
            .watcher(model.getWatcher())
            .content(model.getContent())
            .build();
    }

    private String sessionKey(UUID sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String contentSessionsZsetKey(UUID contentId) {
        return CONTENT_SESSIONS_ZSET_PREFIX + contentId + CONTENT_SESSIONS_ZSET_SUFFIX;
    }

    private String watcherCurrentKey(UUID watcherId) {
        return WATCHER_CURRENT_PREFIX + watcherId + WATCHER_CURRENT_SUFFIX;
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
