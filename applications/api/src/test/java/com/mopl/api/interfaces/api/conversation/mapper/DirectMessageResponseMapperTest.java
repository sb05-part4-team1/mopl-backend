package com.mopl.api.interfaces.api.conversation.mapper;

import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.fixture.DirectMessageModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
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

            UserSummary senderSummary = new UserSummary(
                directMessageModel.getSender().getId(),
                directMessageModel.getSender().getName(),
                "https://cdn.example.com/sender.jpg"
            );
            UserSummary receiverSummary = new UserSummary(
                directMessageModel.getReceiver().getId(),
                directMessageModel.getReceiver().getName(),
                "https://cdn.example.com/receiver.jpg"
            );

            given(userSummaryMapper.toSummary(directMessageModel.getSender())).willReturn(senderSummary);
            given(userSummaryMapper.toSummary(directMessageModel.getReceiver())).willReturn(receiverSummary);

            // when
            DirectMessageResponse result = mapper.toResponse(directMessageModel);

            // then
            assertThat(result.id()).isEqualTo(directMessageModel.getId());
            assertThat(result.conversationId()).isEqualTo(conversationId);
            assertThat(result.createdAt()).isEqualTo(directMessageModel.getCreatedAt());
            assertThat(result.sender()).isEqualTo(senderSummary);
            assertThat(result.receiver()).isEqualTo(receiverSummary);
            assertThat(result.content()).isEqualTo(directMessageModel.getContent());
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            DirectMessageResponse result = mapper.toResponse(null);

            // then
            assertThat(result).isNull();
        }
    }
}
