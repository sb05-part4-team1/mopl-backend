package com.mopl.websocket.application.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.user.UserSummaryMapper;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeType;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentChatFacade {

    private static final String WATCH_DESTINATION_PREFIX = "/sub/contents/";
    private static final String WATCH_DESTINATION_SUFFIX = "/watch";

    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionRepository watchingSessionRepository;
    private final UserSummaryMapper userSummaryMapper;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final WebSocketBroadcaster webSocketBroadcaster;

    public ContentChatResponse sendChatMessage(UUID userId, UUID contentId, String message) {
        ensureWatchingSession(contentId, userId);
        UserModel sender = userService.getById(userId);
        return new ContentChatResponse(userSummaryMapper.toSummary(sender), message);
    }

    private void ensureWatchingSession(UUID contentId, UUID userId) {
        Optional<WatchingSessionModel> existingOpt = watchingSessionRepository.findByWatcherId(userId);

        if (existingOpt.isPresent()) {
            WatchingSessionModel existing = existingOpt.get();
            if (existing.getContentId().equals(contentId)) {
                return;
            }
            broadcastLeave(existing);
            watchingSessionRepository.delete(existing);
        }

        WatchingSessionModel session = createNewSession(contentId, userId);
        watchingSessionRepository.save(session);
        broadcastJoin(session);
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

    private void broadcastJoin(WatchingSessionModel session) {
        WatchingSessionChangeResponse response = new WatchingSessionChangeResponse(
            WatchingSessionChangeType.JOIN,
            watchingSessionResponseMapper.toResponse(session),
            watchingSessionRepository.countByContentId(session.getContentId())
        );
        webSocketBroadcaster.broadcast(buildWatchDestination(session.getContentId()), response);
    }

    private void broadcastLeave(WatchingSessionModel session) {
        WatchingSessionChangeResponse response = new WatchingSessionChangeResponse(
            WatchingSessionChangeType.LEAVE,
            watchingSessionResponseMapper.toResponse(session),
            watchingSessionRepository.countByContentId(session.getContentId()) - 1
        );
        webSocketBroadcaster.broadcast(buildWatchDestination(session.getContentId()), response);
    }

    private String buildWatchDestination(UUID contentId) {
        return WATCH_DESTINATION_PREFIX + contentId + WATCH_DESTINATION_SUFFIX;
    }
}
