package com.mopl.websocket.application.content;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.api.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.api.interfaces.api.watchingsession.mapper.WatchingSessionResponseMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.websocket.interfaces.api.content.ChangeType;
import com.mopl.websocket.interfaces.api.content.ContentChatDto;
import com.mopl.websocket.interfaces.api.content.WatchingSessionChange;
import com.mopl.websocket.service.content.WebSocketWatchingSessionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentWebSocketFacade {

    private final WebSocketWatchingSessionService webSocketWatchingSessionService;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final UserSummaryMapper userSummaryMapper;

    public WatchingSessionChange updateSession(UUID contentId, UUID userId, ChangeType type) {
        UserModel watcher = userService.getById(userId);
        ContentModel content = contentService.getById(contentId);
        WatchingSessionModel session = WatchingSessionModel.create(watcher, content);
        WatchingSessionModel dtoTarget;

        if (type == ChangeType.JOIN) {
            dtoTarget = webSocketWatchingSessionService.create(session);
        } else {
            dtoTarget = webSocketWatchingSessionService.findCurrentByWatcherId(userId)
                .orElse(session); // 혹시 없으면 fallback(없으면 id/createdAt null일 수 있음)

            webSocketWatchingSessionService.delete(session);
        }

        WatchingSessionResponse dto = watchingSessionResponseMapper.toDto(dtoTarget, watcher, content, List.of());
        long watcherCount = webSocketWatchingSessionService.getWatcherCount(contentId);

        return new WatchingSessionChange(type, dto, watcherCount);
    }

    public ContentChatDto sendChatMessage(UUID contentId, UUID userId, String message) {
        UserModel sender = userService.getById(userId);

        return new ContentChatDto(
            userSummaryMapper.toSummary(sender),
            message
        );
    }
}
