package com.mopl.websocket.application.content;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.domain.exception.watchingsession.WatchingSessionNotFoundException;
import com.mopl.websocket.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.websocket.interfaces.api.watchingsession.dto.WatchingSessionResponse;
import com.mopl.websocket.interfaces.api.watchingsession.mapper.WatchingSessionResponseMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.websocket.interfaces.api.content.ChangeType;
import com.mopl.websocket.interfaces.api.content.ContentChatDto;
import com.mopl.websocket.interfaces.api.content.WatchingSessionChange;
import com.mopl.websocket.application.watchingsession.WatchingSessionWebSocketFacade;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentWebSocketFacade {

    private final WatchingSessionWebSocketFacade watchingSessionWebSocketFacade;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final UserSummaryMapper userSummaryMapper;

    public WatchingSessionChange updateSession(UUID contentId, UUID userId, ChangeType type) {
        WatchingSessionModel session;

        if (type == ChangeType.JOIN) {
            UserModel watcher = userService.getById(userId);
            ContentModel content = contentService.getById(contentId);
            session = WatchingSessionModel.create(
                watcher.getId(),
                watcher.getName(),
                watcher.getProfileImagePath(),
                content.getId(),
                content.getTitle()
            );
            session = watchingSessionWebSocketFacade.create(session);
        } else {
            session = watchingSessionWebSocketFacade.findCurrentByWatcherId(userId)
                .orElseThrow(() -> WatchingSessionNotFoundException.withUserIdAndContentId(userId, contentId));
            watchingSessionWebSocketFacade.delete(session);
        }

        WatchingSessionResponse dto = watchingSessionResponseMapper.toDto(session);
        long watcherCount = watchingSessionWebSocketFacade.getWatcherCount(contentId);

        return new WatchingSessionChange(type, dto, watcherCount);
    }

    public ContentChatDto sendChatMessage(UUID contentId, UUID userId, String message) {
        UserModel sender = userService.getById(userId);
        return new ContentChatDto(userSummaryMapper.toSummary(sender), message);
    }
}
