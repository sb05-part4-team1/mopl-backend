package com.mopl.websocket.interfaces.event.content;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.model.user.UserModel;
import com.mopl.dto.content.ContentSummary;
import com.mopl.dto.user.UserSummary;
import com.mopl.dto.watchingsession.WatchingSessionResponse;
import com.mopl.redis.pubsub.WebSocketMessagePublisher;
import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.websocket.application.content.WatchingSessionFacade;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeType;
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
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchingSessionEventListener 테스트")
class WatchingSessionEventListenerTest {

    @Mock
    private WatchingSessionFacade watchingSessionFacade;

    @Mock
    private WebSocketMessagePublisher webSocketMessagePublisher;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<WatchingSessionChangeResponse> responseCaptor;

    private WatchingSessionEventListener listener;

    private UUID userId;
    private UUID contentId;
    private MoplUserDetails userDetails;

    @BeforeEach
    void setUp() {
        listener = new WatchingSessionEventListener(watchingSessionFacade, webSocketMessagePublisher);

        userId = UUID.randomUUID();
        contentId = UUID.randomUUID();
        userDetails = MoplUserDetails.builder()
            .userId(userId)
            .role(UserModel.Role.USER)
            .createdAt(Instant.now())
            .email("test@example.com")
            .name("테스트 사용자")
            .locked(false)
            .build();
    }

    @Nested
    @DisplayName("handleSubscribe() - 구독 이벤트 처리")
    class HandleSubscribeTest {

        @Test
        @DisplayName("유효한 watch 구독 시 세션 참가 처리 및 메시지 전송")
        void withValidWatchSubscription_joinsSessionAndSendsMessage() {
            // given
            String destination = "/sub/contents/" + contentId + "/watch";
            SessionSubscribeEvent event = createSubscribeEvent(destination);

            UserSummary userSummary = new UserSummary(userId, "테스트 사용자", null);
            ContentSummary contentSummary = new ContentSummary(
                contentId, ContentType.movie, "테스트 영화", "설명", null, null, 0.0, 0
            );
            WatchingSessionResponse sessionResponse = new WatchingSessionResponse(
                UUID.randomUUID(), Instant.now(), userSummary, contentSummary
            );
            WatchingSessionChangeResponse changeResponse = new WatchingSessionChangeResponse(
                WatchingSessionChangeType.JOIN, sessionResponse, 1L
            );

            given(watchingSessionFacade.joinSession(contentId, userId)).willReturn(changeResponse);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).should().joinSession(contentId, userId);
            then(webSocketMessagePublisher).should().publish(
                destinationCaptor.capture(),
                responseCaptor.capture()
            );

            assertThat(destinationCaptor.getValue()).isEqualTo(destination);
            assertThat(responseCaptor.getValue()).isEqualTo(changeResponse);
        }

        @Test
        @DisplayName("watch 이외의 destination 구독 시 무시")
        void withNonWatchDestination_ignores() {
            // given
            String destination = "/sub/contents/" + contentId + "/chat";
            SessionSubscribeEvent event = createSubscribeEvent(destination);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).should(never()).joinSession(contentId, userId);
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("destination이 null인 경우 무시")
        void withNullDestination_ignores() {
            // given
            SessionSubscribeEvent event = createSubscribeEvent(null);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("user가 null인 경우 무시")
        void withNullUser_ignores() {
            // given
            String destination = "/sub/contents/" + contentId + "/watch";
            SessionSubscribeEvent event = createSubscribeEventWithoutUser(destination);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("sessionAttributes가 null인 경우 무시")
        void withNullSessionAttributes_ignores() {
            // given
            String destination = "/sub/contents/" + contentId + "/watch";
            SessionSubscribeEvent event = createSubscribeEventWithoutSessionAttributes(destination);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("잘못된 형식의 contentId인 경우 무시")
        void withInvalidContentId_ignores() {
            // given
            String destination = "/sub/contents/invalid-uuid/watch";
            SessionSubscribeEvent event = createSubscribeEvent(destination);

            // when
            listener.handleSubscribe(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("handleUnsubscribe() - 구독 취소 이벤트 처리")
    class HandleUnsubscribeTest {

        @Test
        @DisplayName("세션에 contentId가 있는 경우 세션 퇴장 처리 및 메시지 전송")
        void withContentIdInSession_leavesSessionAndSendsMessage() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("watchingContentId", contentId);
            SessionUnsubscribeEvent event = createUnsubscribeEvent(sessionAttributes);

            UserSummary userSummary = new UserSummary(userId, "테스트 사용자", null);
            ContentSummary contentSummary = new ContentSummary(
                contentId, ContentType.movie, "테스트 영화", "설명", null, null, 0.0, 0
            );
            WatchingSessionResponse sessionResponse = new WatchingSessionResponse(
                UUID.randomUUID(), Instant.now(), userSummary, contentSummary
            );
            WatchingSessionChangeResponse changeResponse = new WatchingSessionChangeResponse(
                WatchingSessionChangeType.LEAVE, sessionResponse, 0L
            );

            given(watchingSessionFacade.leaveSession(contentId, userId)).willReturn(changeResponse);

            // when
            listener.handleUnsubscribe(event);

            // then
            then(watchingSessionFacade).should().leaveSession(contentId, userId);
            then(webSocketMessagePublisher).should().publish(
                destinationCaptor.capture(),
                responseCaptor.capture()
            );

            assertThat(destinationCaptor.getValue()).isEqualTo("/sub/contents/" + contentId + "/watch");
            assertThat(responseCaptor.getValue()).isEqualTo(changeResponse);
        }

        @Test
        @DisplayName("세션에 contentId가 없는 경우 무시")
        void withoutContentIdInSession_ignores() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            SessionUnsubscribeEvent event = createUnsubscribeEvent(sessionAttributes);

            // when
            listener.handleUnsubscribe(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("facade가 null을 반환하면 메시지 전송하지 않음")
        void withNullFacadeResponse_doesNotSendMessage() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("watchingContentId", contentId);
            SessionUnsubscribeEvent event = createUnsubscribeEvent(sessionAttributes);

            given(watchingSessionFacade.leaveSession(contentId, userId)).willReturn(null);

            // when
            listener.handleUnsubscribe(event);

            // then
            then(watchingSessionFacade).should().leaveSession(contentId, userId);
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("handleDisconnect() - 연결 종료 이벤트 처리")
    class HandleDisconnectTest {

        @Test
        @DisplayName("세션에 contentId가 있는 경우 세션 퇴장 처리 및 메시지 전송")
        void withContentIdInSession_leavesSessionAndSendsMessage() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("watchingContentId", contentId);
            SessionDisconnectEvent event = createDisconnectEvent(sessionAttributes);

            UserSummary userSummary = new UserSummary(userId, "테스트 사용자", null);
            ContentSummary contentSummary = new ContentSummary(
                contentId, ContentType.movie, "테스트 영화", "설명", null, null, 0.0, 0
            );
            WatchingSessionResponse sessionResponse = new WatchingSessionResponse(
                UUID.randomUUID(), Instant.now(), userSummary, contentSummary
            );
            WatchingSessionChangeResponse changeResponse = new WatchingSessionChangeResponse(
                WatchingSessionChangeType.LEAVE, sessionResponse, 0L
            );

            given(watchingSessionFacade.leaveSession(contentId, userId)).willReturn(changeResponse);

            // when
            listener.handleDisconnect(event);

            // then
            then(watchingSessionFacade).should().leaveSession(contentId, userId);
            then(webSocketMessagePublisher).should().publish(
                destinationCaptor.capture(),
                responseCaptor.capture()
            );

            assertThat(destinationCaptor.getValue()).isEqualTo("/sub/contents/" + contentId + "/watch");
            assertThat(responseCaptor.getValue()).isEqualTo(changeResponse);
        }

        @Test
        @DisplayName("세션에 contentId가 없는 경우 무시")
        void withoutContentIdInSession_ignores() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            SessionDisconnectEvent event = createDisconnectEvent(sessionAttributes);

            // when
            listener.handleDisconnect(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("sessionAttributes가 null인 경우 무시")
        void withNullSessionAttributes_ignores() {
            // given
            SessionDisconnectEvent event = createDisconnectEventWithoutSessionAttributes();

            // when
            listener.handleDisconnect(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("user가 null인 경우 무시")
        void withNullUser_ignores() {
            // given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("watchingContentId", contentId);
            SessionDisconnectEvent event = createDisconnectEventWithoutUser(sessionAttributes);

            // when
            listener.handleDisconnect(event);

            // then
            then(watchingSessionFacade).shouldHaveNoInteractions();
            then(webSocketMessagePublisher).shouldHaveNoInteractions();
        }
    }

    private SessionSubscribeEvent createSubscribeEvent(String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionSubscribeEvent(this, message);
    }

    private SessionSubscribeEvent createSubscribeEventWithoutUser(String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionSubscribeEvent(this, message);
    }

    private SessionSubscribeEvent createSubscribeEventWithoutSessionAttributes(String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionSubscribeEvent(this, message);
    }

    private SessionUnsubscribeEvent createUnsubscribeEvent(Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        accessor.setSessionAttributes(sessionAttributes);
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionUnsubscribeEvent(this, message);
    }

    @SuppressWarnings("DataFlowIssue")
    private SessionDisconnectEvent createDisconnectEvent(Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionAttributes(sessionAttributes);
        accessor.setSessionId("session-id");
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, "session-id", null);
    }

    @SuppressWarnings("DataFlowIssue")
    private SessionDisconnectEvent createDisconnectEventWithoutSessionAttributes() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("session-id");
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, "session-id", null);
    }

    @SuppressWarnings("DataFlowIssue")
    private SessionDisconnectEvent createDisconnectEventWithoutUser(Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionAttributes(sessionAttributes);
        accessor.setSessionId("session-id");

        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, "session-id", null);
    }
}
