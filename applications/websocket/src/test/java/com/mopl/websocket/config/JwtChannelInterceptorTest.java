package com.mopl.websocket.config;

import com.mopl.domain.exception.auth.InvalidTokenException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.jwt.provider.JwtPayload;
import com.mopl.security.jwt.provider.JwtProvider;
import com.mopl.security.jwt.provider.TokenType;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtChannelInterceptor 단위 테스트")
class JwtChannelInterceptorTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private MessageChannel channel;

    private JwtChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtChannelInterceptor(jwtProvider);
    }

    @Nested
    @DisplayName("CONNECT 명령어")
    class ConnectCommandTest {

        @Test
        @DisplayName("유효한 토큰으로 연결 시 인증 정보 설정")
        @SuppressWarnings("ConstantConditions")
        void withValidToken_setsAuthentication() {
            // given
            UUID userId = UUID.randomUUID();
            String token = "valid.jwt.token";

            JwtPayload payload = new JwtPayload(
                userId,
                UUID.randomUUID(),
                new Date(),
                new Date(System.currentTimeMillis() + 3600000),
                UserModel.Role.USER
            );

            given(jwtProvider.verifyAndParse(eq(token), eq(TokenType.ACCESS))).willReturn(payload);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "Bearer " + token);
            accessor.setLeaveMutable(true);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            Principal user = resultAccessor.getUser();

            assertThat(user).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) user;
            MoplUserDetails userDetails = (MoplUserDetails) auth.getPrincipal();
            assertThat(userDetails.userId()).isEqualTo(userId);
            assertThat(userDetails.role()).isEqualTo(UserModel.Role.USER);
        }

        @Test
        @DisplayName("토큰 없이 연결 시 예외 발생")
        void withoutToken_throwsException() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when & then
            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("인증 토큰이 필요합니다");
        }

        @Test
        @DisplayName("Bearer 접두사 없는 토큰으로 연결 시 예외 발생")
        void withoutBearerPrefix_throwsException() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "invalid.token");
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when & then
            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("인증 토큰이 필요합니다");
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 연결 시 예외 발생")
        void withInvalidToken_throwsException() {
            // given
            String invalidToken = "invalid.jwt.token";

            given(jwtProvider.verifyAndParse(eq(invalidToken), eq(TokenType.ACCESS)))
                .willThrow(InvalidTokenException.create());

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "Bearer " + invalidToken);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when & then
            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("유효하지 않은 토큰");
        }
    }

    @Nested
    @DisplayName("SUBSCRIBE 명령어")
    class SubscribeCommandTest {

        @Test
        @DisplayName("watch destination 구독 시 세션에 contentId 저장")
        void withWatchDestination_storesContentIdInSession() {
            // given
            UUID contentId = UUID.randomUUID();
            String destination = "/sub/contents/" + contentId + "/watch";

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination(destination);
            Map<String, Object> sessionAttributes = new HashMap<>();
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
            assertThat(sessionAttributes.get("watchingContentId")).isEqualTo(contentId);
        }

        @Test
        @DisplayName("유효하지 않은 contentId로 watch 구독 시 예외 발생")
        void withInvalidContentId_throwsException() {
            // given
            String destination = "/sub/contents/invalid-uuid/watch";

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination(destination);
            accessor.setSessionAttributes(new HashMap<>());
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when & then
            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("유효하지 않은 콘텐츠 ID");
        }

        @Test
        @DisplayName("watch가 아닌 destination 구독 시 세션 변경 없음")
        void withNonWatchDestination_doesNotModifySession() {
            // given
            String destination = "/sub/contents/" + UUID.randomUUID() + "/chat";

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination(destination);
            Map<String, Object> sessionAttributes = new HashMap<>();
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
            assertThat(sessionAttributes).doesNotContainKey("watchingContentId");
        }

        @Test
        @DisplayName("destination 없이 구독 시 정상 처리")
        void withoutDestination_passesThrough() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("기타 명령어")
    class OtherCommandTest {

        @Test
        @DisplayName("SEND 명령어는 그대로 통과")
        void withSendCommand_passesThrough() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setDestination("/app/chat");
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(message);
        }

        @Test
        @DisplayName("DISCONNECT 명령어는 그대로 통과")
        void withDisconnectCommand_passesThrough() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("accessor가 null인 경우 메시지 그대로 반환")
        void withNullAccessor_returnsOriginalMessage() {
            // given
            Message<?> message = MessageBuilder.withPayload(new byte[0]).build();

            // when
            Message<?> result = interceptor.preSend(message, channel);

            // then
            assertThat(result).isSameAs(message);
        }
    }
}
