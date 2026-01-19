package com.mopl.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.mopl.websocket.monitoring.WebSocketMetrics;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor; // 기존 JWT 인증 인터셉터
    private final WebSocketMetrics webSocketMetrics; // WebSocket 메트릭 업데이트 객체

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 구독 경로
        registry.setApplicationDestinationPrefixes("/pub"); // 발행(서버로 전송) 경로
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 웹소켓 엔드포인트
                .setAllowedOriginPatterns("*") // CORS 허용(현재는 전체)
                .withSockJS(); // SockJS 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트 -> 서버로 들어오는 메시지 채널에 JWT 인증 인터셉터 적용
        registration.interceptors(jwtChannelInterceptor);

        // 클라이언트 -> 서버로 들어오는 모든 메시지를 카운팅
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                webSocketMetrics.onInboundMessage(); // inbound 메시지 +1
                return message; // 메시지는 그대로 통과
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 서버 -> 클라이언트로 나가는 모든 메시지를 카운팅
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                webSocketMetrics.onOutboundMessage(); // outbound 메시지 +1
                return message; // 메시지는 그대로 통과
            }
        });
    }
}
