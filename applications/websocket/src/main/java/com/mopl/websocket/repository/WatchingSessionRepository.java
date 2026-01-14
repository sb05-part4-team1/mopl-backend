package com.mopl.websocket.repository;

import java.util.Optional;
import java.util.UUID;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

public interface WatchingSessionRepository {

    void save(WatchingSessionModel model);

    void delete(UUID userId, UUID contentId);

    Optional<WatchingSessionModel> findByUserIdAndContentId(UUID userId, UUID contentId);

    long countByContentId(UUID contentId);
}
