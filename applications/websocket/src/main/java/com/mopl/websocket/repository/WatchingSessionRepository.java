package com.mopl.websocket.repository;

import java.util.Optional;
import java.util.UUID;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

public interface WatchingSessionRepository {

    WatchingSessionModel save(WatchingSessionModel model);

    void delete(WatchingSessionModel model);

    Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId);

    long countByContentId(UUID contentId);
}
