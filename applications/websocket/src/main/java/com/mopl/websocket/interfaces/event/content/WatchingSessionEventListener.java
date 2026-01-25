package com.mopl.websocket.interfaces.event.content;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.WatchingSessionFacade;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

    private static final String WATCH_DESTINATION_PREFIX = "/sub/contents/";
    private static final String WATCH_DESTINATION_SUFFIX = "/watch";
    private static final String SESSION_ATTR_CONTENT_ID = "watchingContentId";

    private final WatchingSessionFacade watchingSessionFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (!isWatchDestination(destination)) {
            return;
        }

        if (accessor.getUser() == null || accessor.getSessionAttributes() == null) {
            return;
        }

        UUID contentId = parseContentId(destination);
        if (contentId == null) {
            return;
        }

        MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser()).getPrincipal();
        accessor.getSessionAttributes().put(SESSION_ATTR_CONTENT_ID, contentId);

        WatchingSessionChangeResponse change = watchingSessionFacade.joinSession(contentId, user.userId());
        messagingTemplate.convertAndSend(destination, change);
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        processLeave(StompHeaderAccessor.wrap(event.getMessage()));
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        processLeave(StompHeaderAccessor.wrap(event.getMessage()));
    }

    private void processLeave(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null || accessor.getUser() == null) {
            return;
        }

        UUID contentId = (UUID) accessor.getSessionAttributes().remove(SESSION_ATTR_CONTENT_ID);
        if (contentId == null) {
            return;
        }

        MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser()).getPrincipal();
        WatchingSessionChangeResponse change = watchingSessionFacade.leaveSession(contentId, user.userId());

        if (change != null) {
            messagingTemplate.convertAndSend(buildWatchDestination(contentId), change);
        }
    }

    private boolean isWatchDestination(String destination) {
        return destination != null
            && destination.startsWith(WATCH_DESTINATION_PREFIX)
            && destination.endsWith(WATCH_DESTINATION_SUFFIX);
    }

    private UUID parseContentId(String destination) {
        try {
            return UUID.fromString(destination.split("/")[3]);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String buildWatchDestination(UUID contentId) {
        return WATCH_DESTINATION_PREFIX + contentId + WATCH_DESTINATION_SUFFIX;
    }
}
