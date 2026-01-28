package com.mopl.websocket.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConnectionMetricsListener 단위 테스트")
class WebSocketConnectionMetricsListenerTest {

    @Mock
    private WebSocketMetrics metrics;

    @Mock
    private SessionConnectEvent connectEvent;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    private WebSocketConnectionMetricsListener listener;

    @BeforeEach
    void setUp() {
        listener = new WebSocketConnectionMetricsListener(metrics);
    }

    @Test
    @DisplayName("연결 이벤트 발생 시 metrics.onConnect() 호출")
    void onConnect_callsMetricsOnConnect() {
        // when
        listener.onConnect(connectEvent);

        // then
        then(metrics).should().onConnect();
    }

    @Test
    @DisplayName("연결 종료 이벤트 발생 시 metrics.onDisconnect() 호출")
    void onDisconnect_callsMetricsOnDisconnect() {
        // when
        listener.onDisconnect(disconnectEvent);

        // then
        then(metrics).should().onDisconnect();
    }
}
