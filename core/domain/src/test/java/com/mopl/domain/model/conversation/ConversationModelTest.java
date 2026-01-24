package com.mopl.domain.model.conversation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConversationModel 단위 테스트")
class ConversationModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("ConversationModel 생성")
        void create_returnsConversationModel() {
            // when
            ConversationModel conversation = ConversationModel.create();

            // then
            assertThat(conversation).isNotNull();
        }
    }
}
