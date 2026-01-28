package com.mopl.websocket.messaging;

import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RequiredArgsConstructor
public class LocalWebSocketBroadcaster implements WebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        LogContext.with("destination", destination).debug("Broadcast to local WebSocket");
    }
}
