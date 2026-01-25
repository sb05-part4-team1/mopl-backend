package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.conversation.mapper.ConversationResponseMapper;
import com.mopl.api.interfaces.api.conversation.mapper.DirectMessageResponseMapper;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.domain.exception.conversation.ConversationAlreadyExistsException;
import com.mopl.domain.exception.conversation.SelfConversationNotAllowedException;
import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationSortField;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageSortField;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationFacade 단위 테스트")
class ConversationFacadeTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private DirectMessageService directMessageService;

    @Mock
    private ReadStatusService readStatusService;

    @Mock
    private UserService userService;

    @Mock
    private ConversationResponseMapper conversationResponseMapper;

    @Mock
    private DirectMessageResponseMapper directMessageResponseMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ConversationFacade conversationFacade;

    private UUID requesterId;
    private UserModel requester;
    private UserModel otherUser;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        requester = UserModelFixture.builder()
            .set("id", requesterId)
            .sample();
        otherUser = UserModelFixture.create();
    }

    @Nested
    @DisplayName("getConversations()")
    class GetConversationsTest {

        @Test
        @DisplayName("대화 목록이 비어있으면 빈 CursorResponse 반환")
        void withEmptyConversations_returnsEmptyCursorResponse() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 20, SortDirection.DESCENDING, ConversationSortField.createdAt
            );
            CursorResponse<ConversationModel> emptyResponse = CursorResponse.empty(
                "createdAt", SortDirection.DESCENDING
            );

            given(conversationService.getAll(requesterId, request)).willReturn(emptyResponse);

            // when
            CursorResponse<ConversationResponse> result = conversationFacade.getConversations(requesterId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();

            then(readStatusService).shouldHaveNoInteractions();
            then(directMessageService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("대화 목록 조회 성공")
        void withConversations_returnsConversationResponses() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 20, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            CursorResponse<ConversationModel> serviceResponse = CursorResponse.of(
                List.of(conversation),
                null, null, false, 1L,
                "createdAt", SortDirection.DESCENDING
            );

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now().minusSeconds(100));
            ReadStatusModel otherReadStatus = createReadStatus(otherUser, conversation, Instant.now());

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)
                .set("createdAt", Instant.now())
                .sample();

            ConversationResponse expectedResponse = new ConversationResponse(
                conversationId,
                new UserSummary(otherUser.getId(), otherUser.getName(), null),
                null,
                true
            );

            given(conversationService.getAll(requesterId, request)).willReturn(serviceResponse);
            given(readStatusService.getReadStatusMap(eq(requesterId), any()))
                .willReturn(Map.of(conversationId, requesterReadStatus));
            given(readStatusService.getOtherReadStatusMapWithParticipant(eq(requesterId), any()))
                .willReturn(Map.of(conversationId, otherReadStatus));
            given(directMessageService.getLastDirectMessageMapWithSender(any()))
                .willReturn(Map.of(conversationId, lastMessage));
            given(conversationResponseMapper.toResponse(conversation, otherUser, lastMessage, true))
                .willReturn(expectedResponse);

            // when
            CursorResponse<ConversationResponse> result = conversationFacade.getConversations(requesterId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().id()).isEqualTo(conversationId);
        }

        @Test
        @DisplayName("상대방이 없는 대화도 정상 처리 (withUser = null)")
        void withNoOtherParticipant_handlesGracefully() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 20, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            CursorResponse<ConversationModel> serviceResponse = CursorResponse.of(
                List.of(conversation),
                null, null, false, 1L,
                "createdAt", SortDirection.DESCENDING
            );

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());

            given(conversationService.getAll(requesterId, request)).willReturn(serviceResponse);
            given(readStatusService.getReadStatusMap(eq(requesterId), any()))
                .willReturn(Map.of(conversationId, requesterReadStatus));
            given(readStatusService.getOtherReadStatusMapWithParticipant(eq(requesterId), any()))
                .willReturn(Map.of());  // 상대방 없음
            given(directMessageService.getLastDirectMessageMapWithSender(any()))
                .willReturn(Map.of());  // 메시지 없음
            given(conversationResponseMapper.toResponse(conversation, null, null, false))
                .willReturn(new ConversationResponse(conversationId, null, null, false));

            // when
            CursorResponse<ConversationResponse> result = conversationFacade.getConversations(requesterId, request);

            // then
            assertThat(result.data()).hasSize(1);
            then(conversationResponseMapper).should().toResponse(conversation, null, null, false);
        }
    }

    @Nested
    @DisplayName("getConversation()")
    class GetConversationTest {

        @Test
        @DisplayName("대화 상세 조회 성공")
        void withValidRequest_returnsConversationResponse() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());
            ReadStatusModel otherReadStatus = createReadStatus(otherUser, conversation, Instant.now());

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)
                .sample();

            ConversationResponse expectedResponse = new ConversationResponse(
                conversationId,
                new UserSummary(otherUser.getId(), otherUser.getName(), null),
                null,
                false
            );

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(requesterReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(otherReadStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);
            given(conversationResponseMapper.toResponse(eq(conversation), eq(otherUser), eq(lastMessage), any(Boolean.class)))
                .willReturn(expectedResponse);

            // when
            ConversationResponse result = conversationFacade.getConversation(requesterId, conversationId);

            // then
            assertThat(result.id()).isEqualTo(conversationId);

            then(readStatusService).should().getReadStatus(requesterId, conversationId);
            then(conversationService).should().getById(conversationId);
        }

        @Test
        @DisplayName("마지막 메시지가 없어도 정상 처리")
        void withNoLastMessage_handlesGracefully() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(requesterReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId)).willReturn(null);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(null);
            given(conversationResponseMapper.toResponse(conversation, null, null, false))
                .willReturn(new ConversationResponse(conversationId, null, null, false));

            // when
            ConversationResponse result = conversationFacade.getConversation(requesterId, conversationId);

            // then
            assertThat(result.id()).isEqualTo(conversationId);
            assertThat(result.hasUnread()).isFalse();
        }
    }

    @Nested
    @DisplayName("getConversationWith()")
    class GetConversationWithTest {

        @Test
        @DisplayName("특정 사용자와의 대화 조회 성공")
        void withValidRequest_returnsConversationResponse() {
            // given
            UUID withUserId = otherUser.getId();
            ConversationModel conversation = ConversationModelFixture.create();

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());

            ConversationResponse expectedResponse = new ConversationResponse(
                conversation.getId(),
                new UserSummary(withUserId, otherUser.getName(), null),
                null,
                false
            );

            given(userService.getById(withUserId)).willReturn(otherUser);
            given(conversationService.getByParticipants(requesterId, withUserId)).willReturn(conversation);
            given(readStatusService.getReadStatus(requesterId, conversation.getId())).willReturn(requesterReadStatus);
            given(directMessageService.getLastDirectMessage(conversation.getId())).willReturn(null);
            given(conversationResponseMapper.toResponse(conversation, otherUser, null, false))
                .willReturn(expectedResponse);

            // when
            ConversationResponse result = conversationFacade.getConversationWith(requesterId, withUserId);

            // then
            assertThat(result.id()).isEqualTo(conversation.getId());

            then(userService).should().getById(withUserId);
            then(conversationService).should().getByParticipants(requesterId, withUserId);
        }
    }

    @Nested
    @DisplayName("createConversation()")
    class CreateConversationTest {

        @Test
        @DisplayName("대화 생성 성공")
        void withValidRequest_createsConversation() {
            // given
            UUID withUserId = otherUser.getId();
            ConversationCreateRequest request = new ConversationCreateRequest(withUserId);

            ConversationModel createdConversation = ConversationModelFixture.create();

            ConversationResponse expectedResponse = new ConversationResponse(
                createdConversation.getId(),
                new UserSummary(withUserId, otherUser.getName(), null),
                null,
                false
            );

            given(conversationService.existsByParticipants(requesterId, withUserId)).willReturn(false);
            given(userService.getById(requesterId)).willReturn(requester);
            given(userService.getById(withUserId)).willReturn(otherUser);
            willAnswer(invocation -> invocation.<TransactionCallback<?>>getArgument(0)
                .doInTransaction(mock(TransactionStatus.class)))
                .given(transactionTemplate).execute(any());
            given(conversationService.create(any(ConversationModel.class))).willReturn(createdConversation);
            given(readStatusService.create(any(ReadStatusModel.class))).willAnswer(inv -> inv.getArgument(0));
            given(conversationResponseMapper.toResponse(createdConversation, otherUser))
                .willReturn(expectedResponse);

            // when
            ConversationResponse result = conversationFacade.createConversation(requesterId, request);

            // then
            assertThat(result.id()).isEqualTo(createdConversation.getId());

            then(conversationService).should().create(any(ConversationModel.class));
            then(readStatusService).should(times(2)).create(any(ReadStatusModel.class));
        }

        @Test
        @DisplayName("자기 자신과 대화 생성 시 SelfConversationNotAllowedException 발생")
        void withSelfConversation_throwsSelfConversationNotAllowedException() {
            // given
            ConversationCreateRequest request = new ConversationCreateRequest(requesterId);

            // when & then
            assertThatThrownBy(() -> conversationFacade.createConversation(requesterId, request))
                .isInstanceOf(SelfConversationNotAllowedException.class);

            then(conversationService).shouldHaveNoInteractions();
            then(userService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("이미 존재하는 대화 생성 시 ConversationAlreadyExistsException 발생")
        void withExistingConversation_throwsConversationAlreadyExistsException() {
            // given
            UUID withUserId = otherUser.getId();
            ConversationCreateRequest request = new ConversationCreateRequest(withUserId);

            given(conversationService.existsByParticipants(requesterId, withUserId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> conversationFacade.createConversation(requesterId, request))
                .isInstanceOf(ConversationAlreadyExistsException.class);

            then(userService).shouldHaveNoInteractions();
            then(transactionTemplate).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getDirectMessages()")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("DM 목록 조회 성공")
        void withValidRequest_returnsDirectMessages() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 20, SortDirection.DESCENDING, DirectMessageSortField.createdAt
            );

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());
            ReadStatusModel otherReadStatus = createReadStatus(otherUser, conversation, Instant.now());

            DirectMessageModel directMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", requester)
                .sample();

            CursorResponse<DirectMessageModel> serviceResponse = CursorResponse.of(
                List.of(directMessage),
                null, null, false, 1L,
                "createdAt", SortDirection.DESCENDING
            );

            DirectMessageResponse expectedMessageResponse = new DirectMessageResponse(
                directMessage.getId(),
                conversationId,
                directMessage.getCreatedAt(),
                new UserSummary(requesterId, requester.getName(), null),
                new UserSummary(otherUser.getId(), otherUser.getName(), null),
                directMessage.getContent()
            );

            given(readStatusService.getReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(requesterReadStatus);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(otherReadStatus);
            given(directMessageService.getDirectMessages(conversationId, request))
                .willReturn(serviceResponse);
            given(directMessageResponseMapper.toResponse(directMessage, otherUser))
                .willReturn(expectedMessageResponse);

            // when
            CursorResponse<DirectMessageResponse> result = conversationFacade.getDirectMessages(
                requesterId, conversationId, request
            );

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().sender().userId()).isEqualTo(requesterId);
        }

        @Test
        @DisplayName("상대방이 보낸 메시지의 receiver는 requester")
        void withMessageFromOther_receiverIsRequester() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 20, SortDirection.DESCENDING, DirectMessageSortField.createdAt
            );

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now());
            ReadStatusModel otherReadStatus = createReadStatus(otherUser, conversation, Instant.now());

            DirectMessageModel directMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)  // 상대방이 보낸 메시지
                .sample();

            CursorResponse<DirectMessageModel> serviceResponse = CursorResponse.of(
                List.of(directMessage),
                null, null, false, 1L,
                "createdAt", SortDirection.DESCENDING
            );

            given(readStatusService.getReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(requesterReadStatus);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(otherReadStatus);
            given(directMessageService.getDirectMessages(conversationId, request))
                .willReturn(serviceResponse);
            given(directMessageResponseMapper.toResponse(directMessage, requester))
                .willReturn(new DirectMessageResponse(
                    directMessage.getId(), conversationId, directMessage.getCreatedAt(),
                    new UserSummary(otherUser.getId(), otherUser.getName(), null),
                    new UserSummary(requesterId, requester.getName(), null),
                    directMessage.getContent()
                ));

            // when
            conversationFacade.getDirectMessages(requesterId, conversationId, request);

            // then
            then(directMessageResponseMapper).should().toResponse(directMessage, requester);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsReadTest {

        @Test
        @DisplayName("읽음 처리 성공")
        void withValidRequest_marksAsRead() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            Instant messageCreatedAt = Instant.now();
            Instant lastReadAt = messageCreatedAt.minusSeconds(100);

            ReadStatusModel readStatus = createReadStatus(requester, conversation, lastReadAt);

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)
                .set("createdAt", messageCreatedAt)
                .sample();

            ReadStatusModel updatedReadStatus = readStatus.updateLastReadAt(messageCreatedAt);

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(readStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);
            given(readStatusService.update(any(ReadStatusModel.class))).willReturn(updatedReadStatus);

            // when
            conversationFacade.markAsRead(requesterId, conversationId);

            // then
            then(readStatusService).should().update(any(ReadStatusModel.class));
        }

        @Test
        @DisplayName("마지막 메시지가 없으면 업데이트하지 않음")
        void withNoLastMessage_doesNotUpdate() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            ReadStatusModel readStatus = createReadStatus(requester, conversation, Instant.now());

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(readStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(null);

            // when
            conversationFacade.markAsRead(requesterId, conversationId);

            // then
            then(readStatusService).should(never()).update(any());
        }

        @Test
        @DisplayName("마지막 메시지가 내가 보낸 것이면 업데이트하지 않음")
        void withLastMessageFromRequester_doesNotUpdate() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            ReadStatusModel readStatus = createReadStatus(requester, conversation, Instant.now());

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", requester)  // 내가 보낸 메시지
                .sample();

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(readStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);

            // when
            conversationFacade.markAsRead(requesterId, conversationId);

            // then
            then(readStatusService).should(never()).update(any());
        }

        @Test
        @DisplayName("이미 읽은 메시지면 업데이트하지 않음")
        void withAlreadyReadMessage_doesNotUpdate() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.builder()
                .set("id", conversationId)
                .sample();

            Instant messageCreatedAt = Instant.now().minusSeconds(100);
            Instant lastReadAt = Instant.now();  // 이미 더 최근에 읽음

            ReadStatusModel readStatus = createReadStatus(requester, conversation, lastReadAt);

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)
                .set("createdAt", messageCreatedAt)
                .sample();

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(readStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);

            // when
            conversationFacade.markAsRead(requesterId, conversationId);

            // then
            then(readStatusService).should(never()).update(any());
        }
    }

    @Nested
    @DisplayName("calculateHasUnread() - getConversation을 통한 간접 테스트")
    class CalculateHasUnreadTest {

        @Test
        @DisplayName("상대방이 보낸 메시지가 lastReadAt 이후면 hasUnread = true")
        void withUnreadMessage_hasUnreadIsTrue() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            Instant lastReadAt = Instant.now().minusSeconds(100);
            Instant messageCreatedAt = Instant.now();

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, lastReadAt);
            ReadStatusModel otherReadStatus = createReadStatus(otherUser, conversation, Instant.now());

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", otherUser)  // 상대방이 보냄
                .set("createdAt", messageCreatedAt)  // lastReadAt 이후
                .sample();

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(requesterReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId))
                .willReturn(otherReadStatus);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);
            given(conversationResponseMapper.toResponse(eq(conversation), eq(otherUser), eq(lastMessage), eq(true)))
                .willReturn(new ConversationResponse(conversationId, null, null, true));

            // when
            conversationFacade.getConversation(requesterId, conversationId);

            // then
            then(conversationResponseMapper).should()
                .toResponse(eq(conversation), eq(otherUser), eq(lastMessage), eq(true));
        }

        @Test
        @DisplayName("내가 보낸 메시지면 hasUnread = false")
        void withMessageFromMe_hasUnreadIsFalse() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            ReadStatusModel requesterReadStatus = createReadStatus(requester, conversation, Instant.now().minusSeconds(100));

            DirectMessageModel lastMessage = DirectMessageModelFixture.builder()
                .set("conversation", conversation)
                .set("sender", requester)  // 내가 보냄
                .set("createdAt", Instant.now())
                .sample();

            given(readStatusService.getReadStatus(requesterId, conversationId)).willReturn(requesterReadStatus);
            given(conversationService.getById(conversationId)).willReturn(conversation);
            given(readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId)).willReturn(null);
            given(directMessageService.getLastDirectMessage(conversationId)).willReturn(lastMessage);
            given(conversationResponseMapper.toResponse(eq(conversation), any(), eq(lastMessage), eq(false)))
                .willReturn(new ConversationResponse(conversationId, null, null, false));

            // when
            conversationFacade.getConversation(requesterId, conversationId);

            // then
            then(conversationResponseMapper).should()
                .toResponse(eq(conversation), any(), eq(lastMessage), eq(false));
        }
    }

    private ReadStatusModel createReadStatus(UserModel participant, ConversationModel conversation, Instant lastReadAt) {
        return ReadStatusModel.builder()
            .id(UUID.randomUUID())
            .lastReadAt(lastReadAt)
            .participant(participant)
            .conversation(conversation)
            .createdAt(Instant.now())
            .build();
    }
}
