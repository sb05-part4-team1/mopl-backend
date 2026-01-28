package com.mopl.websocket.messaging;

import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisWebSocketBroadcaster 단위 테스트")
class RedisWebSocketBroadcasterTest {

    @Mock
    private WebSocketMessagePublisher webSocketMessagePublisher;

    private RedisWebSocketBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new RedisWebSocketBroadcaster(webSocketMessagePublisher);
    }

    @Test
    @DisplayName("broadcast() 호출 시 WebSocketMessagePublisher.publish() 위임")
    void broadcast_delegatesToPublisher() {
        // given
        String destination = "/sub/contents/123/watch";
        Map<String, Object> payload = Map.of("type", "USER_JOINED", "userId", "user-1");

        // when
        broadcaster.broadcast(destination, payload);

        // then
        then(webSocketMessagePublisher).should().publish(destination, payload);
    }

    @Test
    @DisplayName("다양한 destination과 payload로 broadcast 가능")
    void broadcast_withVariousDestinations() {
        // given
        String destination = "/sub/conversations/456/messages";
        String payload = "simple string payload";

        // when
        broadcaster.broadcast(destination, payload);

        // then
        then(webSocketMessagePublisher).should().publish(destination, payload);
    }
}
