package com.mopl.websocket.repository;

import java.util.UUID;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

public interface WatchingSessionRepository {

    WatchingSessionModel save(WatchingSessionModel model);

    void delete(WatchingSessionModel model);

    long countByContentId(UUID contentId);
}
