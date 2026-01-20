package com.mopl.websocket.monitoring;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketConnectionMetricsListener {

    private final WebSocketMetrics metrics; // 메트릭 업데이트 담당

    public WebSocketConnectionMetricsListener(WebSocketMetrics metrics) {
        this.metrics = metrics;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        metrics.onConnect(); // 연결 이벤트 발생 시 +1
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        metrics.onDisconnect(); // 연결 종료 이벤트 발생 시 -1
    }
}
