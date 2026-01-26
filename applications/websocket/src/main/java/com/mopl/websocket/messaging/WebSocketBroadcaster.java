package com.mopl.websocket.messaging;

public interface WebSocketBroadcaster {

    void broadcast(String destination, Object payload);
}
