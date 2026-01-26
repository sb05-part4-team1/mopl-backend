package com.mopl.websocket.interfaces.api.conversation;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.user.UserSummary;
import com.mopl.websocket.application.conversation.DirectMessageFacade;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageController 슬라이스 테스트")
class DirectMessageControllerTest {

    @Mock
    private DirectMessageFacade directMessageFacade;

    @Mock
    private WebSocketBroadcaster webSocketBroadcaster;

    @Mock
    private Principal principal;

    private DirectMessageController directMessageController;

    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        directMessageController = new DirectMessageController(directMessageFacade, webSocketBroadcaster);

        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        given(principal.getName()).willReturn(senderId.toString());
    }

    @Nested
    @DisplayName("sendDirectMessage() - DM 전송")
    class SendDirectMessageTest {

        @Test
        @DisplayName("유효한 요청 시 WebSocket 메시지 발행")
        void withValidRequest_publishesWebSocketMessage() {
            // given
            String content = "안녕하세요";
            DirectMessageSendRequest request = new DirectMessageSendRequest(content);

            UUID messageId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserSummary senderSummary = new UserSummary(senderId, "Sender", null);
            UserSummary receiverSummary = new UserSummary(receiverId, "Receiver", null);

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                messageId,
                conversationId,
                createdAt,
                senderSummary,
                receiverSummary,
                content
            );

            given(directMessageFacade.sendDirectMessage(senderId, conversationId, request))
                .willReturn(expectedResponse);

            // when
            directMessageController.sendDirectMessage(principal, conversationId, request);

            // then
            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);

            ArgumentCaptor<DirectMessageResponse> responseCaptor = ArgumentCaptor.forClass(DirectMessageResponse.class);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/conversations/" + conversationId + "/direct-messages"),
                responseCaptor.capture()
            );

            DirectMessageResponse publishedResponse = responseCaptor.getValue();
            assertThat(publishedResponse.id()).isEqualTo(messageId);
            assertThat(publishedResponse.conversationId()).isEqualTo(conversationId);
            assertThat(publishedResponse.sender()).isEqualTo(senderSummary);
            assertThat(publishedResponse.receiver()).isEqualTo(receiverSummary);
            assertThat(publishedResponse.content()).isEqualTo(content);
        }

        @Test
        @DisplayName("빈 내용의 메시지 전송 시에도 facade 호출 및 메시지 발행")
        void withEmptyContent_callsFacadeAndPublishes() {
            // given
            String content = "";
            DirectMessageSendRequest request = new DirectMessageSendRequest(content);

            UUID messageId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserSummary senderSummary = new UserSummary(senderId, "Sender", null);
            UserSummary receiverSummary = new UserSummary(receiverId, "Receiver", null);

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                messageId,
                conversationId,
                createdAt,
                senderSummary,
                receiverSummary,
                content
            );

            given(directMessageFacade.sendDirectMessage(senderId, conversationId, request))
                .willReturn(expectedResponse);

            // when
            directMessageController.sendDirectMessage(principal, conversationId, request);

            // then
            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/conversations/" + conversationId + "/direct-messages"),
                eq(expectedResponse)
            );
        }

        @Test
        @DisplayName("수신자가 없는 경우에도 메시지 발행")
        void withNoReceiver_publishesMessageWithNullReceiver() {
            // given
            String content = "혼잣말";
            DirectMessageSendRequest request = new DirectMessageSendRequest(content);

            UUID messageId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserSummary senderSummary = new UserSummary(senderId, "Sender", null);

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                messageId,
                conversationId,
                createdAt,
                senderSummary,
                null,
                content
            );

            given(directMessageFacade.sendDirectMessage(senderId, conversationId, request))
                .willReturn(expectedResponse);

            // when
            directMessageController.sendDirectMessage(principal, conversationId, request);

            // then
            ArgumentCaptor<DirectMessageResponse> responseCaptor = ArgumentCaptor.forClass(DirectMessageResponse.class);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/conversations/" + conversationId + "/direct-messages"),
                responseCaptor.capture()
            );

            assertThat(responseCaptor.getValue().receiver()).isNull();
            assertThat(responseCaptor.getValue().sender()).isEqualTo(senderSummary);
        }

        @Test
        @DisplayName("긴 메시지 내용도 정상 처리")
        void withLongContent_publishesMessage() {
            // given
            String content = "가".repeat(1000);
            DirectMessageSendRequest request = new DirectMessageSendRequest(content);

            UUID messageId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserSummary senderSummary = new UserSummary(senderId, "Sender", null);
            UserSummary receiverSummary = new UserSummary(receiverId, "Receiver", null);

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                messageId,
                conversationId,
                createdAt,
                senderSummary,
                receiverSummary,
                content
            );

            given(directMessageFacade.sendDirectMessage(senderId, conversationId, request))
                .willReturn(expectedResponse);

            // when
            directMessageController.sendDirectMessage(principal, conversationId, request);

            // then
            ArgumentCaptor<DirectMessageResponse> responseCaptor = ArgumentCaptor.forClass(DirectMessageResponse.class);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/conversations/" + conversationId + "/direct-messages"),
                responseCaptor.capture()
            );

            assertThat(responseCaptor.getValue().content()).hasSize(1000);
        }
    }
}
