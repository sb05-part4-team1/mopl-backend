package com.mopl.websocket.interfaces.api.conversation;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.user.UserSummary;
import com.mopl.websocket.application.conversation.DirectMessageFacade;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageController 슬라이스 테스트")
class DirectMessageControllerTest {

    @Mock
    private DirectMessageFacade directMessageFacade;

    @Mock
    private Principal principal;

    private DirectMessageController directMessageController;

    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        directMessageController = new DirectMessageController(directMessageFacade);

        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        given(principal.getName()).willReturn(senderId.toString());
    }

    @Nested
    @DisplayName("sendDirectMessage() - DM 전송")
    class SendDirectMessageTest {

        @Test
        @DisplayName("유효한 요청 시 DirectMessageResponse 반환")
        void withValidRequest_returnsDirectMessageResponse() {
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
            DirectMessageResponse result = directMessageController.sendDirectMessage(
                principal,
                conversationId,
                request
            );

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.id()).isEqualTo(messageId);
            assertThat(result.conversationId()).isEqualTo(conversationId);
            assertThat(result.sender()).isEqualTo(senderSummary);
            assertThat(result.receiver()).isEqualTo(receiverSummary);
            assertThat(result.content()).isEqualTo(content);

            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);
        }

        @Test
        @DisplayName("빈 내용의 메시지 전송 시에도 facade 호출")
        void withEmptyContent_callsFacade() {
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
            DirectMessageResponse result = directMessageController.sendDirectMessage(
                principal,
                conversationId,
                request
            );

            // then
            assertThat(result.content()).isEmpty();
            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);
        }

        @Test
        @DisplayName("수신자가 없는 경우에도 응답 반환")
        void withNoReceiver_returnsResponseWithNullReceiver() {
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
            DirectMessageResponse result = directMessageController.sendDirectMessage(
                principal,
                conversationId,
                request
            );

            // then
            assertThat(result.receiver()).isNull();
            assertThat(result.sender()).isEqualTo(senderSummary);
            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);
        }

        @Test
        @DisplayName("긴 메시지 내용도 정상 처리")
        void withLongContent_returnsResponse() {
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
            DirectMessageResponse result = directMessageController.sendDirectMessage(
                principal,
                conversationId,
                request
            );

            // then
            assertThat(result.content()).hasSize(1000);
            then(directMessageFacade).should().sendDirectMessage(senderId, conversationId, request);
        }
    }
}
