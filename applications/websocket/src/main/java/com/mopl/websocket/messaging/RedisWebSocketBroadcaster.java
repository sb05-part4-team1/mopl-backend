package com.mopl.websocket.messaging;

import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisWebSocketBroadcaster implements WebSocketBroadcaster {

    private final WebSocketMessagePublisher webSocketMessagePublisher;

    @Override
    public void broadcast(String destination, Object payload) {
        webSocketMessagePublisher.publish(destination, payload);
        LogContext.with("destination", destination).debug("Broadcast to Redis Pub/Sub");
    }
}
