package com.mopl.websocket.service.content;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.websocket.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchingSessionService {

    private final WatchingSessionRepository watchingSessionRepository;

    public void create(WatchingSessionModel model) {
        watchingSessionRepository.save(model);
    }

    public void delete(UUID userId, UUID contentId) {
        watchingSessionRepository.delete(userId, contentId);
    }

    public long getWatcherCount(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }

    public Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId) {
        return watchingSessionRepository.findByUserIdAndContentId(userId, contentId);
    }
}
