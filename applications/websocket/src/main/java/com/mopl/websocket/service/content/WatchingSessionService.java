package com.mopl.websocket.service.content;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.websocket.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchingSessionService {

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
}
