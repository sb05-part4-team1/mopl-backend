package com.mopl.websocket.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.userdetails.MoplUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor == null)
            return message;

        // 연결 시점 토큰 검증 및 유저 설정
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                try {
                    JwtPayload payload = jwtProvider.verifyAndParse(token, TokenType.ACCESS);

                    MoplUserDetails userDetails = MoplUserDetails.builder()
                        .userId(payload.sub())
                        .role(payload.role())
                        .build();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                    // 세션에 저장
                    accessor.setUser(authentication);
                } catch (Exception e) {
                    return null;
                }
            }
        }

        // 메시지 전송 시점에 세션의 유저 정보를 SecurityContext에 주입
        else if (accessor.getUser() != null) {
            Authentication authentication = (Authentication) accessor.getUser();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        return message;
    }
}
