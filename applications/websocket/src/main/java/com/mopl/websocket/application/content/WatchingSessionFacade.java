package com.mopl.websocket.application.content;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchingSessionFacade {

    private final WatchingSessionRepository watchingSessionRepository;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;

    public WatchingSessionChangeResponse joinSession(UUID contentId, UUID userId) {
        Optional<WatchingSessionModel> existingOpt = watchingSessionRepository.findByWatcherId(userId);

        WatchingSessionModel session;
        if (existingOpt.isPresent()) {
            WatchingSessionModel existing = existingOpt.get();
            if (existing.getContentId().equals(contentId)) {
                session = existing.incrementConnectionCount();
            } else {
                watchingSessionRepository.delete(existing);
                session = createNewSession(contentId, userId);
            }
        } else {
            session = createNewSession(contentId, userId);
        }
        watchingSessionRepository.save(session);

        return new WatchingSessionChangeResponse(
            WatchingSessionChangeType.JOIN,
            watchingSessionResponseMapper.toDto(session),
            watchingSessionRepository.countByContentId(contentId)
        );
    }

    public WatchingSessionChangeResponse leaveSession(UUID contentId, UUID userId) {
        Optional<WatchingSessionModel> existingOpt = watchingSessionRepository.findByWatcherId(userId);

        if (existingOpt.isEmpty() || !existingOpt.get().getContentId().equals(contentId)) {
            return null;
        }

        WatchingSessionModel session = existingOpt.get().decrementConnectionCount();

        if (session.hasNoConnections()) {
            watchingSessionRepository.delete(existingOpt.get());
            return new WatchingSessionChangeResponse(
                WatchingSessionChangeType.LEAVE,
                watchingSessionResponseMapper.toDto(existingOpt.get()),
                watchingSessionRepository.countByContentId(contentId)
            );
        }

        watchingSessionRepository.save(session);
        return null;
    }

    private WatchingSessionModel createNewSession(UUID contentId, UUID userId) {
        ContentModel content = contentService.getById(contentId);
        UserModel watcher = userService.getById(userId);
        return WatchingSessionModel.create(
            watcher.getId(),
            watcher.getName(),
            watcher.getProfileImagePath(),
            content.getId(),
            content.getTitle()
        );
    }
}
