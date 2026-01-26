package com.mopl.websocket.messaging;

import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RedisWebSocketBroadcaster implements WebSocketBroadcaster {

    private final WebSocketMessagePublisher webSocketMessagePublisher;

    @Override
    public void broadcast(String destination, Object payload) {
        webSocketMessagePublisher.publish(destination, payload);
        log.debug("Broadcast to Redis Pub/Sub: {}", destination);
    }
}
