package com.mopl.domain.service.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class WatchingSessionService {

    private final WatchingSessionQueryRepository watchingSessionQueryRepository;
    private final WatchingSessionRepository watchingSessionRepository;

    public Optional<WatchingSessionModel> getWatchingSessionByWatcherId(UUID watcherId) {
        return watchingSessionRepository.findByWatcherId(watcherId);
    }

    public CursorResponse<WatchingSessionModel> getWatchingSessions(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionQueryRepository.findByContentId(contentId, request);
    }

    public long countByContentId(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }

    public Map<UUID, Long> countByContentIdIn(List<UUID> contentIds) {
        return watchingSessionRepository.countByContentIdIn(contentIds);
    }
}
