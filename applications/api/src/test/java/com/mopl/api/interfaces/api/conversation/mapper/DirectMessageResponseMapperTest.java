package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.conversation.DirectMessageResponseMapper;
import com.mopl.dto.user.UserSummary;
import com.mopl.dto.user.UserSummaryMapper;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageResponseMapper 단위 테스트")
class DirectMessageResponseMapperTest {

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @InjectMocks
    private DirectMessageResponseMapper mapper;

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("DirectMessageModel을 DirectMessageResponse로 변환")
        void withDirectMessageModel_returnsDirectMessageResponse() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversationModel = ConversationModel.builder()
                .id(conversationId)
                .build();

            DirectMessageModel directMessageModel = DirectMessageModelFixture.builder()
                .set("conversation", conversationModel)
                .sample();

            UserModel receiver = UserModelFixture.create();

            UserSummary senderSummary = new UserSummary(
                directMessageModel.getSender().getId(),
                directMessageModel.getSender().getName(),
                "https://cdn.example.com/sender.jpg"
            );
            UserSummary receiverSummary = new UserSummary(
                receiver.getId(),
                receiver.getName(),
                "https://cdn.example.com/receiver.jpg"
            );

            given(userSummaryMapper.toSummary(directMessageModel.getSender())).willReturn(senderSummary);
            given(userSummaryMapper.toSummary(receiver)).willReturn(receiverSummary);

            // when
            DirectMessageResponse result = mapper.toResponse(directMessageModel, receiver);

            // then
            assertThat(result.id()).isEqualTo(directMessageModel.getId());
            assertThat(result.conversationId()).isEqualTo(conversationId);
            assertThat(result.createdAt()).isEqualTo(directMessageModel.getCreatedAt());
            assertThat(result.sender()).isEqualTo(senderSummary);
            assertThat(result.receiver()).isEqualTo(receiverSummary);
            assertThat(result.content()).isEqualTo(directMessageModel.getContent());
        }

        @Test
        @DisplayName("directMessageModel이 null인 경우 null 반환")
        void withNullDirectMessageModel_returnsNull() {
            // when
            DirectMessageResponse result = mapper.toResponse(null, null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("receiver가 null인 경우에도 정상 변환")
        void withNullReceiver_returnsDirectMessageResponse() {
            // given
            UUID conversationId = UUID.randomUUID();
            ConversationModel conversationModel = ConversationModel.builder()
                .id(conversationId)
                .build();

            DirectMessageModel directMessageModel = DirectMessageModelFixture.builder()
                .set("conversation", conversationModel)
                .sample();

            UserSummary senderSummary = new UserSummary(
                directMessageModel.getSender().getId(),
                directMessageModel.getSender().getName(),
                "https://cdn.example.com/sender.jpg"
            );

            given(userSummaryMapper.toSummary(directMessageModel.getSender())).willReturn(senderSummary);
            given(userSummaryMapper.toSummary(null)).willReturn(null);

            // when
            DirectMessageResponse result = mapper.toResponse(directMessageModel, null);

            // then
            assertThat(result.id()).isEqualTo(directMessageModel.getId());
            assertThat(result.conversationId()).isEqualTo(conversationId);
            assertThat(result.sender()).isEqualTo(senderSummary);
            assertThat(result.receiver()).isNull();
            assertThat(result.content()).isEqualTo(directMessageModel.getContent());
        }
    }
}
