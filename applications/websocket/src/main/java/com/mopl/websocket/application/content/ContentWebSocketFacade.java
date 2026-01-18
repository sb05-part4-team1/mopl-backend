package com.mopl.websocket.application.content;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.api.interfaces.api.watchingsession.WatchingSessionDto;
import com.mopl.api.interfaces.api.watchingsession.WatchingSessionResponseMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.websocket.interfaces.api.content.ChangeType;
import com.mopl.websocket.interfaces.api.content.ContentChatDto;
import com.mopl.websocket.interfaces.api.content.WatchingSessionChange;
import com.mopl.websocket.service.content.WatchingSessionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentWebSocketFacade {

    private final WatchingSessionService watchingSessionService;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final UserSummaryMapper userSummaryMapper;

    public WatchingSessionChange updateSession(UUID contentId, UUID userId, ChangeType type) {
        WatchingSessionDto dto = null;

        if (type == ChangeType.JOIN) {
            UserModel watcher = userService.getById(userId);
            ContentModel content = contentService.getById(contentId);

            WatchingSessionModel session = WatchingSessionModel.create(watcher, content);
            watchingSessionService.create(session);

            dto = watchingSessionResponseMapper.toDto(session, watcher, content);
        } else {
            Optional<WatchingSessionModel> sessionOpt = watchingSessionService.findByUserIdAndContentId(userId, contentId);

            if (sessionOpt.isPresent()) {
                WatchingSessionModel session = sessionOpt.get();

                watchingSessionService.delete(session);

                dto = watchingSessionResponseMapper.toDto(session, session.getWatcher(), session.getContent());
            }
        }
        long watcherCount = watchingSessionService.getWatcherCount(contentId);

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
