package com.mopl.websocket.config;

import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
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

                    accessor.setUser(authentication);
                } catch (Exception e) {
                    throw new MessageDeliveryException("인증에 실패했습니다.");
                }
            }
        }

        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (StringUtils.hasText(destination) && destination.startsWith("/sub/contents/")
                && destination.endsWith("/watch")) {
                // 경로 파싱: /sub/contents/{contentId}/watch
                String contentIdStr = destination.split("/")[3];
                UUID contentId = UUID.fromString(contentIdStr);

                if (accessor.getSessionAttributes() != null) {
                    accessor.getSessionAttributes().put("watchingContentId", contentId);
                }
            }
        }

        return message;
    }
}
