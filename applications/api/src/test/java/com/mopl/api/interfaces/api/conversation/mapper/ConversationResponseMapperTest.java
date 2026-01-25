package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationResponseMapper 단위 테스트")
class ConversationResponseMapperTest {

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @Mock
    private DirectMessageResponseMapper directMessageResponseMapper;

    @InjectMocks
    private ConversationResponseMapper mapper;

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("ConversationModel과 연관 데이터를 ConversationResponse로 변환")
        void withConversationModelAndRelatedData_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.create();
            UserModel withUser = UserModelFixture.create();
            DirectMessageModel lastMessage = DirectMessageModelFixture.create();
            boolean hasUnread = true;

            UserSummary withUserSummary = new UserSummary(
                withUser.getId(),
                withUser.getName(),
                "https://cdn.example.com/profile.jpg"
            );

            DirectMessageResponse lastMessageResponse = new DirectMessageResponse(
                lastMessage.getId(),
                UUID.randomUUID(),
                Instant.now(),
                withUserSummary,
                withUserSummary,
                lastMessage.getContent()
            );

            given(userSummaryMapper.toSummary(withUser)).willReturn(withUserSummary);
            given(directMessageResponseMapper.toResponse(lastMessage, withUser)).willReturn(lastMessageResponse);

            // when
            ConversationResponse result = mapper.toResponse(
                conversationModel,
                withUser,
                lastMessage,
                hasUnread
            );

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isEqualTo(withUserSummary);
            assertThat(result.lastMessage()).isEqualTo(lastMessageResponse);
            assertThat(result.hasUnread()).isTrue();
        }

        @Test
        @DisplayName("lastMessage가 null인 경우에도 정상 변환")
        void withNullLastMessage_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.create();
            UserModel withUser = UserModelFixture.create();

            UserSummary withUserSummary = new UserSummary(
                withUser.getId(),
                withUser.getName(),
                "https://cdn.example.com/profile.jpg"
            );

            given(userSummaryMapper.toSummary(withUser)).willReturn(withUserSummary);
            given(directMessageResponseMapper.toResponse(null, withUser)).willReturn(null);

            // when
            ConversationResponse result = mapper.toResponse(
                conversationModel,
                withUser,
                null,
                false
            );

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isEqualTo(withUserSummary);
            assertThat(result.lastMessage()).isNull();
            assertThat(result.hasUnread()).isFalse();
        }

        @Test
        @DisplayName("withUser가 null인 경우에도 정상 변환")
        void withNullWithUser_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.create();
            DirectMessageModel lastMessage = DirectMessageModelFixture.create();

            DirectMessageResponse lastMessageResponse = new DirectMessageResponse(
                lastMessage.getId(),
                UUID.randomUUID(),
                Instant.now(),
                null,
                null,
                lastMessage.getContent()
            );

            given(userSummaryMapper.toSummary(null)).willReturn(null);
            given(directMessageResponseMapper.toResponse(lastMessage, null)).willReturn(lastMessageResponse);

            // when
            ConversationResponse result = mapper.toResponse(
                conversationModel,
                null,
                lastMessage,
                false
            );

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isNull();
            assertThat(result.lastMessage()).isEqualTo(lastMessageResponse);
            assertThat(result.hasUnread()).isFalse();
        }

        @Test
        @DisplayName("ConversationModel과 withUser만으로 변환 (lastMessage=null, hasUnread=false)")
        void withConversationModelAndWithUserOnly_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.create();
            UserModel withUser = UserModelFixture.create();

            UserSummary withUserSummary = new UserSummary(
                withUser.getId(),
                withUser.getName(),
                "https://cdn.example.com/profile.jpg"
            );

            given(userSummaryMapper.toSummary(withUser)).willReturn(withUserSummary);
            given(directMessageResponseMapper.toResponse(null, withUser)).willReturn(null);

            // when
            ConversationResponse result = mapper.toResponse(conversationModel, withUser);

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isEqualTo(withUserSummary);
            assertThat(result.lastMessage()).isNull();
            assertThat(result.hasUnread()).isFalse();
        }
    }
}
