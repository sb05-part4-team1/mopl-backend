package com.mopl.api.application.watchingsession;

import com.mopl.dto.watchingsession.WatchingSessionResponse;
import com.mopl.api.interfaces.api.watchingsession.mapper.WatchingSessionResponseMapper;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchingSessionFacade {

    private final WatchingSessionService watchingSessionService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;

    public Optional<WatchingSessionResponse> getWatchingSession(UUID watcherId) {
        return watchingSessionService.getWatchingSessionByWatcherId(watcherId)
            .map(watchingSessionResponseMapper::toDto);
    }

    public CursorResponse<WatchingSessionResponse> getWatchingSessions(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionService.getWatchingSessions(contentId, request)
            .map(watchingSessionResponseMapper::toDto);
    }
}
