package com.mopl.websocket.application.content;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mopl.api.interfaces.api.watchingsession.WatchingSessionDto;
import com.mopl.api.interfaces.api.watchingsession.WatchingSessionResponseMapper;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.websocket.interfaces.api.content.ChangeType;
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

    public WatchingSessionChange updateSession(UUID contentId, UUID userId, ChangeType type) {
        WatchingSessionModel session;
        UserModel watcher;
        ContentModel content;

        if (type == ChangeType.JOIN) {
            watcher = userService.getById(userId);
            content = contentService.getById(contentId);

            session = WatchingSessionModel.create(watcher, content);
            watchingSessionService.create(session);
        } else {
            // LEAVE인 경우 삭제 전에 세션 조회
            session = watchingSessionService.findByUserIdAndContentId(userId, contentId)
                .orElse(null);

            if (session != null) {
                watcher = session.getWatcher();
                content = session.getContent();
            } else {
                watcher = null;
                content = null;
            }

            watchingSessionService.delete(userId, contentId);
        }

        long watcherCount = watchingSessionService.getWatcherCount(contentId);

        WatchingSessionDto dto = (session != null && watcher != null && content != null)
            ? watchingSessionResponseMapper.toDto(session, watcher, content)
            : null;

        return new WatchingSessionChange(type, dto, watcherCount);
    }
}
