package com.mopl.websocket.application.conversation;

import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.conversation.DirectMessageResponseMapper;
import com.mopl.dto.user.UserSummary;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageFacade 단위 테스트")
class DirectMessageFacadeTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private DirectMessageService directMessageService;

    @Mock
    private ReadStatusService readStatusService;

    @Mock
    private DirectMessageResponseMapper directMessageResponseMapper;

    @Mock
    private DirectMessagePublisher directMessagePublisher;

    @InjectMocks
    private DirectMessageFacade directMessageFacade;

    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;
    private UserModel sender;
    private UserModel receiver;
    private ConversationModel conversation;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        conversationId = UUID.randomUUID();

        sender = UserModelFixture.builder()
            .set("id", senderId)
            .set("name", "Sender")
            .sample();

        receiver = UserModelFixture.builder()
            .set("id", receiverId)
            .set("name", "Receiver")
            .sample();

        conversation = ConversationModelFixture.builder()
            .set("id", conversationId)
            .sample();
    }

    @Nested
    @DisplayName("sendDirectMessage()")
    class SendDirectMessageTest {

        @Test
        @DisplayName("DM 전송 성공 및 Redis 발행")
        void withValidRequest_sendsMessageAndPublishesToRedis() {
            // given
            DirectMessageSendRequest request = new DirectMessageSendRequest("Hello");

            ReadStatusModel senderReadStatus = ReadStatusModel.builder()
                .id(UUID.randomUUID())
                .participant(sender)
                .conversation(conversation)
                .lastReadAt(Instant.now())
                .createdAt(Instant.now())
                .build();

            ReadStatusModel receiverReadStatus = ReadStatusModel.builder()
                .id(UUID.randomUUID())
                .participant(receiver)
                .conversation(conversation)
                .lastReadAt(Instant.now())
                .createdAt(Instant.now())
                .build();

            DirectMessageModel savedMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", sender)
                .set("content", "Hello")
                .sample();

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                savedMessage.getId(),
                conversationId,
                savedMessage.getCreatedAt(),
                new UserSummary(senderId, "Sender", null),
                new UserSummary(receiverId, "Receiver", null),
                "Hello"
            );

            given(readStatusService.getReadStatus(senderId, conversationId)).willReturn(senderReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(senderId, conversationId))
                .willReturn(receiverReadStatus);
            given(directMessageService.save(any(DirectMessageModel.class))).willReturn(savedMessage);
            given(directMessageResponseMapper.toResponse(savedMessage, receiver)).willReturn(expectedResponse);

            // when
            DirectMessageResponse result = directMessageFacade.sendDirectMessage(senderId, conversationId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).isEqualTo("Hello");

            then(directMessageService).should().save(any(DirectMessageModel.class));
            then(directMessagePublisher).should().publish(expectedResponse);
        }

        @Test
        @DisplayName("상대방이 없는 대화에서도 DM 전송 및 Redis 발행")
        void withNoReceiver_sendsMessageAndPublishesToRedis() {
            // given
            DirectMessageSendRequest request = new DirectMessageSendRequest("Hello");

            ReadStatusModel senderReadStatus = ReadStatusModel.builder()
                .id(UUID.randomUUID())
                .participant(sender)
                .conversation(conversation)
                .lastReadAt(Instant.now())
                .createdAt(Instant.now())
                .build();

            DirectMessageModel savedMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", sender)
                .set("content", "Hello")
                .sample();

            DirectMessageResponse expectedResponse = new DirectMessageResponse(
                savedMessage.getId(),
                conversationId,
                savedMessage.getCreatedAt(),
                new UserSummary(senderId, "Sender", null),
                null,
                "Hello"
            );

            given(readStatusService.getReadStatus(senderId, conversationId)).willReturn(senderReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(senderId, conversationId))
                .willReturn(null);
            given(directMessageService.save(any(DirectMessageModel.class))).willReturn(savedMessage);
            given(directMessageResponseMapper.toResponse(savedMessage, null)).willReturn(expectedResponse);

            // when
            DirectMessageResponse result = directMessageFacade.sendDirectMessage(senderId, conversationId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.receiver()).isNull();

            then(directMessagePublisher).should().publish(expectedResponse);
        }
    }
}
