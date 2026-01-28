package com.mopl.websocket.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalWebSocketBroadcaster 단위 테스트")
class LocalWebSocketBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private LocalWebSocketBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new LocalWebSocketBroadcaster(messagingTemplate);
    }

    @Test
    @DisplayName("broadcast() 호출 시 SimpMessagingTemplate.convertAndSend() 위임")
    void broadcast_delegatesToMessagingTemplate() {
        // given
        String destination = "/sub/contents/123/watch";
        Map<String, Object> payload = Map.of("type", "USER_JOINED", "userId", "user-1");

        // when
        broadcaster.broadcast(destination, payload);

        // then
        then(messagingTemplate).should().convertAndSend(destination, payload);
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
        then(messagingTemplate).should().convertAndSend(destination, payload);
    }
}
