package com.mopl.api.application.watchingsession;

import com.mopl.api.interfaces.api.watchingsession.WatchingSessionDto;
import com.mopl.api.interfaces.api.watchingsession.WatchingSessionResponseMapper;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRequest;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchingSessionFacade {

    private final UserService userService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final WatchingSessionService watchingSessionService;

    public Optional<WatchingSessionDto> getWatchingSession(
        UUID requesterId,
        UUID watcherId
    ) {
        // (기존 정책 유지) 인증/존재 보장 - 여기서 DB 탐
        userService.getById(requesterId);
        userService.getById(watcherId);

        Optional<WatchingSessionModel> sessionOpt = watchingSessionService
            .getWatchingSessionByWatcherId(watcherId);

        return sessionOpt.map(session -> watchingSessionResponseMapper.toDto(
            session,
            session.getWatcher(),
            session.getContent()
        ));
    }

    public CursorResponse<WatchingSessionDto> getWatchingSessions(
        UUID contentId,
        WatchingSessionQueryRequest request
    ) {
        return watchingSessionService.getWatchingSessions(contentId, request)
            .map(session -> watchingSessionResponseMapper.toDto(
                session,
                session.getWatcher(),
                session.getContent()
            ));
    }
}
