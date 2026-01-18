package com.mopl.websocket.config;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.ContentWebSocketFacade;
import com.mopl.websocket.interfaces.api.content.ChangeType;
import com.mopl.websocket.interfaces.api.content.WatchingSessionChange;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

    private final ContentWebSocketFacade contentWebSocketFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/sub/contents/") && destination.endsWith(
            "/watch")) {
            UUID contentId = UUID.fromString(destination.split("/")[3]);
            MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser())
                .getPrincipal();

            WatchingSessionChange change = contentWebSocketFacade.updateSession(contentId, user
                .userId(), ChangeType.JOIN);
            messagingTemplate.convertAndSend(destination, change);
        }
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
        if (accessor.getSessionAttributes() == null) {
            return;
        }

        UUID contentId = (UUID) accessor.getSessionAttributes().get("watchingContentId");

        if (contentId != null && accessor.getUser() != null) {
            MoplUserDetails user = (MoplUserDetails) ((Authentication) accessor.getUser())
                .getPrincipal();

            WatchingSessionChange change = contentWebSocketFacade.updateSession(contentId, user
                .userId(), ChangeType.LEAVE);

            messagingTemplate.convertAndSend("/sub/contents/" + contentId + "/watch", change);

            accessor.getSessionAttributes().remove("watchingContentId");
        }
    }
}
