package com.mopl.domain.model.conversation;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("DirectMessageModel 단위 테스트")
class DirectMessageModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 DirectMessageModel 생성")
        void withValidData_createsDirectMessage() {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();

            ConversationModel conversation = mock(ConversationModel.class);
            given(conversation.getId()).willReturn(conversationId);

            UserModel sender = mock(UserModel.class);
            given(sender.getId()).willReturn(senderId);

            String content = "안녕하세요";

            // when
            DirectMessageModel directMessage = DirectMessageModel.create(content, sender, conversation);

            // then
            assertThat(directMessage.getContent()).isEqualTo(content);
            assertThat(directMessage.getConversation()).isEqualTo(conversation);
            assertThat(directMessage.getSender()).isEqualTo(sender);
        }

        @Test
        @DisplayName("빈 내용으로도 DirectMessageModel 생성 가능")
        void withEmptyContent_createsDirectMessage() {
            // given
            ConversationModel conversation = mock(ConversationModel.class);
            UserModel sender = mock(UserModel.class);
            String content = "";

            // when
            DirectMessageModel directMessage = DirectMessageModel.create(content, sender, conversation);

            // then
            assertThat(directMessage.getContent()).isEmpty();
            assertThat(directMessage.getConversation()).isEqualTo(conversation);
            assertThat(directMessage.getSender()).isEqualTo(sender);
        }

        @Test
        @DisplayName("null 값으로도 DirectMessageModel 생성 가능")
        void withNullValues_createsDirectMessage() {
            // when
            DirectMessageModel directMessage = DirectMessageModel.create(null, null, null);

            // then
            assertThat(directMessage.getContent()).isNull();
            assertThat(directMessage.getConversation()).isNull();
            assertThat(directMessage.getSender()).isNull();
        }
    }
}
