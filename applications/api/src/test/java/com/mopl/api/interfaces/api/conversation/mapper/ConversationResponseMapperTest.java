package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
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
        @DisplayName("ConversationModel을 ConversationResponse로 변환")
        void withConversationModel_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.create();

            UserSummary withUserSummary = new UserSummary(
                conversationModel.getWithUser().getId(),
                conversationModel.getWithUser().getName(),
                "https://cdn.example.com/profile.jpg"
            );

            DirectMessageResponse lastMessageResponse = new DirectMessageResponse(
                conversationModel.getLastMessage().getId(),
                UUID.randomUUID(),
                Instant.now(),
                withUserSummary,
                withUserSummary,
                conversationModel.getLastMessage().getContent()
            );

            given(userSummaryMapper.toSummary(conversationModel.getWithUser())).willReturn(withUserSummary);
            given(directMessageResponseMapper.toResponse(conversationModel.getLastMessage()))
                .willReturn(lastMessageResponse);

            // when
            ConversationResponse result = mapper.toResponse(conversationModel);

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isEqualTo(withUserSummary);
            assertThat(result.lastMessage()).isEqualTo(lastMessageResponse);
            assertThat(result.hasUnread()).isEqualTo(conversationModel.isHasUnread());
        }

        @Test
        @DisplayName("lastMessage가 null인 경우에도 정상 변환")
        void withNullLastMessage_returnsConversationResponse() {
            // given
            ConversationModel conversationModel = ConversationModelFixture.createWithoutLastMessage();

            UserSummary withUserSummary = new UserSummary(
                conversationModel.getWithUser().getId(),
                conversationModel.getWithUser().getName(),
                "https://cdn.example.com/profile.jpg"
            );

            given(userSummaryMapper.toSummary(conversationModel.getWithUser())).willReturn(withUserSummary);
            given(directMessageResponseMapper.toResponse(null)).willReturn(null);

            // when
            ConversationResponse result = mapper.toResponse(conversationModel);

            // then
            assertThat(result.id()).isEqualTo(conversationModel.getId());
            assertThat(result.with()).isEqualTo(withUserSummary);
            assertThat(result.lastMessage()).isNull();
            assertThat(result.hasUnread()).isEqualTo(conversationModel.isHasUnread());
        }
    }
}
