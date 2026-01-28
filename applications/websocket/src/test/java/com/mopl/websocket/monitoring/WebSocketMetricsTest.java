package com.mopl.websocket.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WebSocketMetrics 단위 테스트")
class WebSocketMetricsTest {

    private MeterRegistry meterRegistry;
    private WebSocketMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new WebSocketMetrics(meterRegistry);
    }

    @Nested
    @DisplayName("onConnect()")
    class OnConnectTest {

        @Test
        @DisplayName("연결 시 activeConnections 증가")
        void incrementsActiveConnections() {
            // when
            metrics.onConnect();

            // then
            Double value = meterRegistry.get("mopl.websocket.connections.active").gauge().value();
            assertThat(value).isEqualTo(1.0);
        }

        @Test
        @DisplayName("여러 번 연결 시 누적 증가")
        void multipleConnections_incrementsCumulatively() {
            // when
            metrics.onConnect();
            metrics.onConnect();
            metrics.onConnect();

            // then
            Double value = meterRegistry.get("mopl.websocket.connections.active").gauge().value();
            assertThat(value).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("onDisconnect()")
    class OnDisconnectTest {

        @Test
        @DisplayName("연결 종료 시 activeConnections 감소")
        void decrementsActiveConnections() {
            // given
            metrics.onConnect();
            metrics.onConnect();

            // when
            metrics.onDisconnect();

            // then
            Double value = meterRegistry.get("mopl.websocket.connections.active").gauge().value();
            assertThat(value).isEqualTo(1.0);
        }

        @Test
        @DisplayName("activeConnections가 음수가 되면 0으로 리셋")
        void preventsNegativeConnections() {
            // when - 연결 없이 종료 호출
            metrics.onDisconnect();

            // then
            Double value = meterRegistry.get("mopl.websocket.connections.active").gauge().value();
            assertThat(value).isEqualTo(0.0);
        }

        @Test
        @DisplayName("연속 종료 시에도 0 이하로 내려가지 않음")
        void multipleDisconnects_staysAtZero() {
            // given
            metrics.onConnect();

            // when
            metrics.onDisconnect();
            metrics.onDisconnect();
            metrics.onDisconnect();

            // then
            Double value = meterRegistry.get("mopl.websocket.connections.active").gauge().value();
            assertThat(value).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("onInboundMessage()")
    class OnInboundMessageTest {

        @Test
        @DisplayName("inbound 메시지 카운터 증가")
        void incrementsInboundCounter() {
            // when
            metrics.onInboundMessage();

            // then
            Counter counter = meterRegistry.get("mopl.websocket.messages.inbound.total").counter();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("여러 번 호출 시 누적 증가")
        void multipleMessages_incrementsCumulatively() {
            // when
            metrics.onInboundMessage();
            metrics.onInboundMessage();
            metrics.onInboundMessage();

            // then
            Counter counter = meterRegistry.get("mopl.websocket.messages.inbound.total").counter();
            assertThat(counter.count()).isEqualTo(3.0);
        }
    }

    @Nested
    @DisplayName("onOutboundMessage()")
    class OnOutboundMessageTest {

        @Test
        @DisplayName("outbound 메시지 카운터 증가")
        void incrementsOutboundCounter() {
            // when
            metrics.onOutboundMessage();

            // then
            Counter counter = meterRegistry.get("mopl.websocket.messages.outbound.total").counter();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("여러 번 호출 시 누적 증가")
        void multipleMessages_incrementsCumulatively() {
            // when
            metrics.onOutboundMessage();
            metrics.onOutboundMessage();

            // then
            Counter counter = meterRegistry.get("mopl.websocket.messages.outbound.total").counter();
            assertThat(counter.count()).isEqualTo(2.0);
        }
    }

    @Nested
    @DisplayName("통합 시나리오")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("연결/메시지/종료 전체 흐름")
        void fullConnectionLifecycle() {
            // given - 사용자 연결
            metrics.onConnect();
            metrics.onConnect();

            // when - 메시지 송수신
            metrics.onInboundMessage();
            metrics.onInboundMessage();
            metrics.onOutboundMessage();

            // then - 메트릭 확인
            assertThat(meterRegistry.get("mopl.websocket.connections.active").gauge().value())
                .isEqualTo(2.0);
            assertThat(meterRegistry.get("mopl.websocket.messages.inbound.total").counter().count())
                .isEqualTo(2.0);
            assertThat(meterRegistry.get("mopl.websocket.messages.outbound.total").counter().count())
                .isEqualTo(1.0);

            // when - 사용자 종료
            metrics.onDisconnect();

            // then
            assertThat(meterRegistry.get("mopl.websocket.connections.active").gauge().value())
                .isEqualTo(1.0);
        }
    }
}
