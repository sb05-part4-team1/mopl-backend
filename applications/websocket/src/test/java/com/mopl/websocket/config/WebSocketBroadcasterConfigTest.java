package com.mopl.websocket.config;

import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import com.mopl.websocket.messaging.LocalWebSocketBroadcaster;
import com.mopl.websocket.messaging.RedisWebSocketBroadcaster;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("WebSocketBroadcasterConfig 단위 테스트")
class WebSocketBroadcasterConfigTest {

    private final WebSocketBroadcasterConfig config = new WebSocketBroadcasterConfig();

    @Test
    @DisplayName("redisWebSocketBroadcaster - Redis 브로드캐스터 빈 생성")
    void redisWebSocketBroadcaster_createsRedisBean() {
        // given
        WebSocketMessagePublisher publisher = mock(WebSocketMessagePublisher.class);

        // when
        WebSocketBroadcaster broadcaster = config.redisWebSocketBroadcaster(publisher);

        // then
        assertThat(broadcaster).isNotNull();
        assertThat(broadcaster).isInstanceOf(RedisWebSocketBroadcaster.class);
    }

    @Test
    @DisplayName("localWebSocketBroadcaster - Local 브로드캐스터 빈 생성")
    void localWebSocketBroadcaster_createsLocalBean() {
        // given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

        // when
        WebSocketBroadcaster broadcaster = config.localWebSocketBroadcaster(messagingTemplate);

        // then
        assertThat(broadcaster).isNotNull();
        assertThat(broadcaster).isInstanceOf(LocalWebSocketBroadcaster.class);
    }
}
