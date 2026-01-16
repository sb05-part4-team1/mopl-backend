package com.mopl.websocket.application.content;

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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
public class ContentWebSocketFacade {

    private final WatchingSessionService watchingSessionService;
    private final UserService userService;
    private final ContentService contentService;
    private final WatchingSessionResponseMapper watchingSessionResponseMapper;
    private final UserSummaryMapper userSummaryMapper;

    public WatchingSessionChange updateSession(UUID contentId, UUID userId, ChangeType type) {
        WatchingSessionModel session;
        UserModel watcher = null;
        ContentModel content = null;

        if (type == ChangeType.JOIN) {
            watcher = userService.getById(userId);
            content = contentService.getById(contentId);

            session = WatchingSessionModel.create(watcher, content);
            session = watchingSessionService.create(session);
        } else {
            // LEAVE인 경우 삭제 전에 세션 조회
            session = watchingSessionService.findByUserIdAndContentId(userId, contentId)
                .orElse(null);

            if (session != null) {
                watcher = session.getWatcher();
                content = session.getContent();

                watchingSessionService.delete(session);
            }
        }
        long watcherCount = watchingSessionService.getWatcherCount(contentId);

        WatchingSessionDto dto = (session != null && watcher != null && content != null)
            ? watchingSessionResponseMapper.toDto(session, watcher, content)
            : null;

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
