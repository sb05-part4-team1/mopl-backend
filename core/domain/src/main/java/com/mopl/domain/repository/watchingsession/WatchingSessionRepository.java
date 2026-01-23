package com.mopl.domain.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;

import java.util.Optional;
import java.util.UUID;

public interface WatchingSessionRepository {

    Optional<WatchingSessionModel> findByWatcherId(UUID watcherId);

    long countByContentId(UUID contentId);
}
