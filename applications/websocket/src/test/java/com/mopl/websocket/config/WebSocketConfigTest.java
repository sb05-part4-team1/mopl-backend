package com.mopl.websocket.config;

import com.mopl.websocket.config.WebSocketProperties.BroadcasterType;
import com.mopl.websocket.monitoring.WebSocketMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConfig 단위 테스트")
class WebSocketConfigTest {

    @Mock
    private JwtChannelInterceptor jwtChannelInterceptor;

    @Mock
    private WebSocketMetrics webSocketMetrics;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private ChannelRegistration channelRegistration;

    @Captor
    private ArgumentCaptor<ChannelInterceptor[]> interceptorsCaptor;

    private WebSocketConfig config;

    @BeforeEach
    void setUp() {
        WebSocketProperties properties = new WebSocketProperties(
            "http://localhost:3000,http://localhost:8080",
            BroadcasterType.local
        );
        config = new WebSocketConfig(jwtChannelInterceptor, webSocketMetrics, properties);
    }

    @Nested
    @DisplayName("configureMessageBroker()")
    class ConfigureMessageBrokerTest {

        @Test
        @DisplayName("메시지 브로커 설정 - /sub, /pub 프리픽스")
        void configuresMessageBroker() {
            // when
            config.configureMessageBroker(messageBrokerRegistry);

            // then
            then(messageBrokerRegistry).should().enableSimpleBroker("/sub");
            then(messageBrokerRegistry).should().setApplicationDestinationPrefixes("/pub");
        }
    }

    @Nested
    @DisplayName("registerStompEndpoints()")
    class RegisterStompEndpointsTest {

        @Test
        @DisplayName("STOMP 엔드포인트 등록 - /ws, SockJS, CORS 설정")
        void registersStompEndpoints() {
            // given
            StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
            given(stompEndpointRegistry.addEndpoint("/ws")).willReturn(registration);
            given(registration.setAllowedOriginPatterns(any(String[].class))).willReturn(registration);

            // when
            config.registerStompEndpoints(stompEndpointRegistry);

            // then
            then(stompEndpointRegistry).should().addEndpoint("/ws");
            ArgumentCaptor<String[]> originsCaptor = ArgumentCaptor.forClass(String[].class);
            then(registration).should().setAllowedOriginPatterns(originsCaptor.capture());
            then(registration).should().withSockJS();

            String[] capturedOrigins = originsCaptor.getValue();
            assertThat(capturedOrigins).containsExactly("http://localhost:3000", "http://localhost:8080");
        }
    }

    @Nested
    @DisplayName("configureClientInboundChannel()")
    class ConfigureClientInboundChannelTest {

        @Test
        @DisplayName("인바운드 채널에 JWT 인터셉터와 메트릭 인터셉터 추가")
        void configuresInboundChannel() {
            // when
            config.configureClientInboundChannel(channelRegistration);

            // then
            then(channelRegistration).should().interceptors(interceptorsCaptor.capture());
            ChannelInterceptor[] interceptors = interceptorsCaptor.getValue();

            assertThat(interceptors).hasSize(2);
            assertThat(interceptors[0]).isSameAs(jwtChannelInterceptor);
            assertThat(interceptors[1]).isNotNull(); // 메트릭 인터셉터
        }

        @Test
        @DisplayName("메트릭 인터셉터가 onInboundMessage 호출")
        void metricsInterceptor_callsOnInboundMessage() {
            // given
            config.configureClientInboundChannel(channelRegistration);
            then(channelRegistration).should().interceptors(interceptorsCaptor.capture());
            ChannelInterceptor metricsInterceptor = interceptorsCaptor.getValue()[1];

            Message<?> message = MessageBuilder.withPayload("test").build();
            MessageChannel channel = mock(MessageChannel.class);

            // when
            metricsInterceptor.preSend(message, channel);

            // then
            then(webSocketMetrics).should().onInboundMessage();
        }
    }

    @Nested
    @DisplayName("configureClientOutboundChannel()")
    class ConfigureClientOutboundChannelTest {

        @Test
        @DisplayName("아웃바운드 채널에 메트릭 인터셉터 추가")
        void configuresOutboundChannel() {
            // when
            config.configureClientOutboundChannel(channelRegistration);

            // then
            then(channelRegistration).should().interceptors(interceptorsCaptor.capture());
            ChannelInterceptor[] interceptors = interceptorsCaptor.getValue();

            assertThat(interceptors).hasSize(1);
            assertThat(interceptors[0]).isNotNull(); // 메트릭 인터셉터
        }

        @Test
        @DisplayName("메트릭 인터셉터가 onOutboundMessage 호출")
        void metricsInterceptor_callsOnOutboundMessage() {
            // given
            config.configureClientOutboundChannel(channelRegistration);
            then(channelRegistration).should().interceptors(interceptorsCaptor.capture());
            ChannelInterceptor metricsInterceptor = interceptorsCaptor.getValue()[0];

            Message<?> message = MessageBuilder.withPayload("test").build();
            MessageChannel channel = mock(MessageChannel.class);

            // when
            metricsInterceptor.preSend(message, channel);

            // then
            then(webSocketMetrics).should().onOutboundMessage();
        }
    }

}
