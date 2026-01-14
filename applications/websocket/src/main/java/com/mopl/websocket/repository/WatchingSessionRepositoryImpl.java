package com.mopl.websocket.repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

@Repository
public class WatchingSessionRepositoryImpl implements WatchingSessionRepository {

    // TODO: Redis로 변경 필요
    private final Map<UUID, Set<UUID>> contentWatchers = new ConcurrentHashMap<>();
    private final Map<String, WatchingSessionModel> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(WatchingSessionModel model) {
        UUID contentId = model.getContent().getId();
        UUID userId = model.getWatcher().getId();

        contentWatchers.computeIfAbsent(contentId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        sessions.put(generateKey(contentId, userId), model);
    }

    @Override
    public void delete(UUID userId, UUID contentId) {
        Set<UUID> watchers = contentWatchers.get(contentId);
        if (watchers != null) {
            watchers.remove(userId);
            if (watchers.isEmpty()) {
                contentWatchers.remove(contentId);
            }
        }
        sessions.remove(generateKey(contentId, userId));
    }

    @Override
    public Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId) {
        return Optional.ofNullable(sessions.get(generateKey(contentId, userId)));
    }

    @Override
    public long countByContentId(UUID contentId) {
        Set<UUID> watchers = contentWatchers.get(contentId);
        return watchers != null ? watchers.size() : 0L;
    }

    private String generateKey(UUID contentId, UUID userId) {
        return contentId.toString() + ":" + userId.toString();
    }
}
