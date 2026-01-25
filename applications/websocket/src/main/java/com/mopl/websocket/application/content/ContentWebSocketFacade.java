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
import com.mopl.dto.user.UserSummaryMapper;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.websocket.interfaces.api.content.dto.WatchingSessionChangeType;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import com.mopl.websocket.interfaces.api.content.dto.WatchingSessionChangeResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentWebSocketFacade {

    private final WatchingSessionRepository watchingSessionRepository;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final UserSummaryMapper userSummaryMapper;

    public WatchingSessionChangeResponse updateSession(UUID contentId, UUID userId, WatchingSessionChangeType type) {
        ContentModel content = contentService.getById(contentId);

        if (type == WatchingSessionChangeType.JOIN) {
            return handleJoin(content, userId);
        } else {
            return handleLeave(contentId, userId);
        }
    }

    private WatchingSessionChangeResponse handleJoin(ContentModel content, UUID userId) {
        UserModel watcher = userService.getById(userId);

        WatchingSessionModel session = WatchingSessionModel.create(
            watcher.getId(),
            watcher.getName(),
            watcher.getProfileImagePath(),
            content.getId(),
            content.getTitle()
        );
        watchingSessionRepository.save(session);

        return new WatchingSessionChangeResponse(
            WatchingSessionChangeType.JOIN,
            watchingSessionResponseMapper.toDto(session),
            watchingSessionRepository.countByContentId(content.getId())
        );
    }

    private WatchingSessionChangeResponse handleLeave(UUID contentId, UUID userId) {
        Optional<WatchingSessionModel> sessionOpt = watchingSessionRepository.findByWatcherId(userId);

        sessionOpt.ifPresent(watchingSessionRepository::delete);

        return new WatchingSessionChangeResponse(
            WatchingSessionChangeType.LEAVE,
            sessionOpt.map(watchingSessionResponseMapper::toDto).orElse(null),
            watchingSessionRepository.countByContentId(contentId)
        );
    }

    public ContentChatResponse sendChatMessage(UUID userId, String message) {
        UserModel sender = userService.getById(userId);
        return new ContentChatResponse(userSummaryMapper.toSummary(sender), message);
    }
}
