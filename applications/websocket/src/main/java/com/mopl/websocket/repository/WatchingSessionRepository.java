package com.mopl.websocket.repository;

import java.util.Optional;
import java.util.UUID;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

public interface WatchingSessionRepository {

    WatchingSessionModel save(WatchingSessionModel model);

    void delete(WatchingSessionModel model);

    long countByContentId(UUID contentId);

    // [추가] watcher 현재 세션 조회 (LEAVE에서 DTO 만들기 용)
    Optional<WatchingSessionModel> findCurrentByWatcherId(UUID watcherId);
}
