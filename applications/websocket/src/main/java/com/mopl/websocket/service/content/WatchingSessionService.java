package com.mopl.websocket.service.content;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.websocket.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchingSessionService {

    private final WatchingSessionRepository watchingSessionRepository;

    @Caching(put = {
        @CachePut(cacheNames = "watching_sessions",
            key = "#model.content.id + ':' + #model.watcher.id")
    })
    public WatchingSessionModel create(WatchingSessionModel model) {
        return watchingSessionRepository.save(model);
    }

    @CacheEvict(cacheNames = "watching_sessions",
        key = "#model.content.id + ':' + #model.watcher.id")
    public void delete(WatchingSessionModel model) {
        watchingSessionRepository.delete(model);
    }

    public long getWatcherCount(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }

    @Cacheable(cacheNames = "watching_sessions", key = "#contentId + ':' + #userId")
    public Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId) {
        return watchingSessionRepository.findByUserIdAndContentId(userId, contentId);
    }
}
