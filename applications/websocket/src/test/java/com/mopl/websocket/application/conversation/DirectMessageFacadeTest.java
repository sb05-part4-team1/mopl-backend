package com.mopl.websocket.application.conversation;

import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.conversation.DirectMessageResponseMapper;
import com.mopl.dto.user.UserSummary;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.websocket.application.outbox.mapper.DomainEventOutboxMapper;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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

    @Mock
    private OutboxService outboxService;

    @Mock
    private DomainEventOutboxMapper domainEventOutboxMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    private DirectMessageFacade directMessageFacade;

    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;
    private UserModel sender;
    private UserModel receiver;
    private ConversationModel conversation;

    @BeforeEach
    void setUp() {
        directMessageFacade = new DirectMessageFacade(
            conversationService,
            directMessageService,
            readStatusService,
            directMessageResponseMapper,
            directMessagePublisher,
            outboxService,
            domainEventOutboxMapper,
            transactionTemplate
        );

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
        @DisplayName("DM 전송 성공 및 Redis 발행, Outbox 저장")
        void withValidRequest_sendsMessageAndPublishesToRedisAndSavesOutbox() {
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

            OutboxModel outboxModel = mock(OutboxModel.class);

            given(readStatusService.getReadStatusWithParticipant(senderId, conversationId)).willReturn(senderReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(senderId, conversationId))
                .willReturn(receiverReadStatus);
            willAnswer(invocation -> invocation.<TransactionCallback<DirectMessageModel>>getArgument(0)
                .doInTransaction(mock(TransactionStatus.class)))
                .given(transactionTemplate).execute(any());
            given(directMessageService.save(any(DirectMessageModel.class))).willReturn(savedMessage);
            given(domainEventOutboxMapper.toOutboxModel(any())).willReturn(outboxModel);
            given(outboxService.save(outboxModel)).willReturn(outboxModel);
            given(directMessageResponseMapper.toResponse(savedMessage, receiver)).willReturn(expectedResponse);

            // when
            DirectMessageResponse result = directMessageFacade.sendDirectMessage(senderId, conversationId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).isEqualTo("Hello");

            then(directMessageService).should().save(any(DirectMessageModel.class));
            then(outboxService).should().save(outboxModel);
            then(directMessagePublisher).should().publish(expectedResponse);
        }

        @Test
        @DisplayName("상대방이 없는 대화에서는 Outbox 저장하지 않음")
        void withNoReceiver_doesNotSaveOutbox() {
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

            given(readStatusService.getReadStatusWithParticipant(senderId, conversationId)).willReturn(senderReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(senderId, conversationId))
                .willReturn(null);
            willAnswer(invocation -> invocation.<TransactionCallback<DirectMessageModel>>getArgument(0)
                .doInTransaction(mock(TransactionStatus.class)))
                .given(transactionTemplate).execute(any());
            given(directMessageService.save(any(DirectMessageModel.class))).willReturn(savedMessage);
            given(directMessageResponseMapper.toResponse(savedMessage, null)).willReturn(expectedResponse);

            // when
            DirectMessageResponse result = directMessageFacade.sendDirectMessage(senderId, conversationId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.receiver()).isNull();

            then(outboxService).should(never()).save(any());
            then(directMessagePublisher).should().publish(expectedResponse);
        }
    }
}
