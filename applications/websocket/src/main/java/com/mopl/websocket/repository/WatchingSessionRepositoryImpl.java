package com.mopl.websocket.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WatchingSessionRepositoryImpl implements WatchingSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String COUNT_KEY_PREFIX = "watching_count:";

    @Override
    public WatchingSessionModel save(WatchingSessionModel model) {
        redisTemplate.opsForSet().add(COUNT_KEY_PREFIX + model.getContent().getId(),
            model.getWatcher().getId().toString());
        return model;
    }

    @Override
    public void delete(WatchingSessionModel model) {
        redisTemplate.opsForSet().remove(COUNT_KEY_PREFIX + model.getContent().getId(), model.getWatcher().getId().toString());
    }

    @Override
    public long countByContentId(UUID contentId) {
        Long count = redisTemplate.opsForSet().size(COUNT_KEY_PREFIX + contentId);
        return count != null ? count : 0L;
    }

    @Override
    public Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId) {
        // Service 계층의 @Cacheable이 우선하므로 이 메서드는 호출될 일이 거의 없음
        return Optional.empty();
    }
}
