package com.mopl.websocket.service.content;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketWatchingSessionService {

    private final WatchingSessionRepository watchingSessionRepository;

    public WatchingSessionModel create(WatchingSessionModel model) {
        return watchingSessionRepository.save(model);
    }

    public void delete(WatchingSessionModel model) {
        watchingSessionRepository.delete(model);
    }

    public long getWatcherCount(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }

    public Optional<WatchingSessionModel> findCurrentByWatcherId(UUID watcherId) {
        return watchingSessionRepository.findByWatcherId(watcherId);
    }
}
