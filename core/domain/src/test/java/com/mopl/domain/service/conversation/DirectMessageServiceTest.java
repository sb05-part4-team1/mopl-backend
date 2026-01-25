package com.mopl.domain.service.conversation;

import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageService 단위 테스트")
class DirectMessageServiceTest {

    @Mock
    private DirectMessageQueryRepository directMessageQueryRepository;

    @Mock
    private DirectMessageRepository directMessageRepository;

    @InjectMocks
    private DirectMessageService directMessageService;

    @Nested
    @DisplayName("getDirectMessages()")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            UUID conversationId = UUID.randomUUID();
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, null, null, null
            );
            CursorResponse<DirectMessageModel> expectedResponse = CursorResponse.empty(
                "createdAt", SortDirection.DESCENDING
            );

            given(directMessageQueryRepository.findAll(conversationId, request))
                .willReturn(expectedResponse);

            // when
            CursorResponse<DirectMessageModel> result = directMessageService.getDirectMessages(
                conversationId, request
            );

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(directMessageQueryRepository).should().findAll(conversationId, request);
        }
    }

    @Nested
    @DisplayName("getLastDirectMessageMapWithSender()")
    class GetLastDirectMessageMapWithSenderTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            UUID conversationId1 = UUID.randomUUID();
            UUID conversationId2 = UUID.randomUUID();
            List<UUID> conversationIds = List.of(conversationId1, conversationId2);

            DirectMessageModel message1 = DirectMessageModelFixture.create();
            DirectMessageModel message2 = DirectMessageModelFixture.create();
            Map<UUID, DirectMessageModel> expectedMap = Map.of(
                conversationId1, message1,
                conversationId2, message2
            );

            given(directMessageQueryRepository.findLastDirectMessagesWithSenderByConversationIdIn(conversationIds))
                .willReturn(expectedMap);

            // when
            Map<UUID, DirectMessageModel> result = directMessageService.getLastDirectMessageMapWithSender(
                conversationIds
            );

            // then
            assertThat(result).isEqualTo(expectedMap);
            then(directMessageQueryRepository).should()
                .findLastDirectMessagesWithSenderByConversationIdIn(conversationIds);
        }
    }

    @Nested
    @DisplayName("getLastDirectMessage()")
    class GetLastDirectMessageTest {

        @Test
        @DisplayName("마지막 메시지가 존재하면 DirectMessageModel 반환")
        void withExistingMessage_returnsDirectMessageModel() {
            // given
            UUID conversationId = UUID.randomUUID();
            DirectMessageModel lastMessage = DirectMessageModelFixture.create();

            given(directMessageRepository.findLastMessageByConversationId(conversationId))
                .willReturn(Optional.of(lastMessage));

            // when
            DirectMessageModel result = directMessageService.getLastDirectMessage(conversationId);

            // then
            assertThat(result).isEqualTo(lastMessage);
            then(directMessageRepository).should().findLastMessageByConversationId(conversationId);
        }

        @Test
        @DisplayName("마지막 메시지가 없으면 null 반환")
        void withNoMessage_returnsNull() {
            // given
            UUID conversationId = UUID.randomUUID();

            given(directMessageRepository.findLastMessageByConversationId(conversationId))
                .willReturn(Optional.empty());

            // when
            DirectMessageModel result = directMessageService.getLastDirectMessage(conversationId);

            // then
            assertThat(result).isNull();
            then(directMessageRepository).should().findLastMessageByConversationId(conversationId);
        }
    }

    @Nested
    @DisplayName("getLastDirectMessageWithSender()")
    class GetLastDirectMessageWithSenderTest {

        @Test
        @DisplayName("마지막 메시지가 존재하면 sender 포함 DirectMessageModel 반환")
        void withExistingMessage_returnsDirectMessageModelWithSender() {
            // given
            UUID conversationId = UUID.randomUUID();
            DirectMessageModel lastMessage = DirectMessageModelFixture.create();

            given(directMessageRepository.findLastMessageWithSenderByConversationId(conversationId))
                .willReturn(Optional.of(lastMessage));

            // when
            DirectMessageModel result = directMessageService.getLastDirectMessageWithSender(conversationId);

            // then
            assertThat(result).isEqualTo(lastMessage);
            then(directMessageRepository).should().findLastMessageWithSenderByConversationId(conversationId);
        }

        @Test
        @DisplayName("마지막 메시지가 없으면 null 반환")
        void withNoMessage_returnsNull() {
            // given
            UUID conversationId = UUID.randomUUID();

            given(directMessageRepository.findLastMessageWithSenderByConversationId(conversationId))
                .willReturn(Optional.empty());

            // when
            DirectMessageModel result = directMessageService.getLastDirectMessageWithSender(conversationId);

            // then
            assertThat(result).isNull();
            then(directMessageRepository).should().findLastMessageWithSenderByConversationId(conversationId);
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("Repository에 위임하여 저장된 DirectMessageModel 반환")
        void delegatesToRepository() {
            // given
            DirectMessageModel messageToSave = DirectMessageModelFixture.create();
            DirectMessageModel savedMessage = DirectMessageModelFixture.create();

            given(directMessageRepository.save(messageToSave))
                .willReturn(savedMessage);

            // when
            DirectMessageModel result = directMessageService.save(messageToSave);

            // then
            assertThat(result).isEqualTo(savedMessage);
            then(directMessageRepository).should().save(messageToSave);
        }
    }
}
