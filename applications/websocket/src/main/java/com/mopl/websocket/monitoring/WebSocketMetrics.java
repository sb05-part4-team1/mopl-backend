package com.mopl.websocket.monitoring;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class WebSocketMetrics {

    private final AtomicInteger activeConnections; // 현재 연결 수를 담는 값(Gauge)
    private final Counter inboundMessages; // 클라이언트 -> 서버로 들어오는 메시지 수(Counter)
    private final Counter outboundMessages; // 서버 -> 클라이언트로 나가는 메시지 수(Counter)

    public WebSocketMetrics(MeterRegistry meterRegistry) {
        // Gauge: AtomicInteger의 현재 값을 Prometheus가 읽어감
        this.activeConnections = meterRegistry.gauge(
            "mopl.websocket.connections.active",
            new AtomicInteger(0)
        );

        // Counter: 누적 카운트
        this.inboundMessages = meterRegistry.counter("mopl.websocket.messages.inbound.total");
        this.outboundMessages = meterRegistry.counter("mopl.websocket.messages.outbound.total");
    }

    public void onConnect() {
        if (activeConnections != null) {
            activeConnections.incrementAndGet(); // 연결 +1
        }
    }

    public void onDisconnect() {
        if (activeConnections != null) {
            int after = activeConnections.decrementAndGet(); // 연결 -1
            if (after < 0) {
                activeConnections.set(0); // 방어적으로 음수 방지
            }
        }
    }

    public void onInboundMessage() {
        inboundMessages.increment(); // inbound +1
    }

    public void onOutboundMessage() {
        outboundMessages.increment(); // outbound +1
    }
}
