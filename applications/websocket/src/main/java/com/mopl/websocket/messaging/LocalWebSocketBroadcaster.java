package com.mopl.websocket.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
@Slf4j
public class LocalWebSocketBroadcaster implements WebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast to local WebSocket: {}", destination);
    }
}
