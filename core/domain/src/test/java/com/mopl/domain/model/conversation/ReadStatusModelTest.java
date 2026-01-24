package com.mopl.domain.model.conversation;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("ReadStatusModel 단위 테스트")
class ReadStatusModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ReadStatusModel 생성")
        void withValidData_createsReadStatus() {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID participantId = UUID.randomUUID();

            ConversationModel conversation = mock(ConversationModel.class);
            given(conversation.getId()).willReturn(conversationId);

            UserModel participant = mock(UserModel.class);
            given(participant.getId()).willReturn(participantId);

            Instant before = Instant.now();

            // when
            ReadStatusModel readStatus = ReadStatusModel.create(conversation, participant);

            // then
            Instant after = Instant.now();
            assertThat(readStatus.getConversation()).isEqualTo(conversation);
            assertThat(readStatus.getParticipant()).isEqualTo(participant);
            assertThat(readStatus.getLastReadAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("null 값으로도 ReadStatusModel 생성 가능")
        void withNullValues_createsReadStatus() {
            // given
            Instant before = Instant.now();

            // when
            ReadStatusModel readStatus = ReadStatusModel.create(null, null);

            // then
            Instant after = Instant.now();
            assertThat(readStatus.getConversation()).isNull();
            assertThat(readStatus.getParticipant()).isNull();
            assertThat(readStatus.getLastReadAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsReadTest {

        @Test
        @DisplayName("markAsRead 호출 시 lastReadAt이 현재 시간으로 갱신된다")
        void markAsRead_updatesLastReadAt() {
            // given
            Instant pastTime = Instant.now().minusSeconds(3600);
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(pastTime)
                .build();

            Instant before = Instant.now();

            // when
            ReadStatusModel updated = readStatus.markAsRead();

            // then
            Instant after = Instant.now();
            assertThat(updated.getLastReadAt()).isBetween(before, after);
            assertThat(updated.getLastReadAt()).isAfter(pastTime);
        }

        @Test
        @DisplayName("markAsRead는 새로운 객체를 반환한다")
        void markAsRead_returnsNewInstance() {
            // given
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(Instant.now().minusSeconds(3600))
                .build();

            // when
            ReadStatusModel updated = readStatus.markAsRead();

            // then
            assertThat(updated).isNotSameAs(readStatus);
        }

        @Test
        @DisplayName("markAsRead 호출 시 다른 필드는 유지된다")
        void markAsRead_preservesOtherFields() {
            // given
            ConversationModel conversation = mock(ConversationModel.class);
            UserModel participant = mock(UserModel.class);
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(7200);

            ReadStatusModel readStatus = ReadStatusModel.builder()
                .id(id)
                .conversation(conversation)
                .participant(participant)
                .lastReadAt(Instant.now().minusSeconds(3600))
                .createdAt(createdAt)
                .build();

            // when
            ReadStatusModel updated = readStatus.markAsRead();

            // then
            assertThat(updated.getId()).isEqualTo(id);
            assertThat(updated.getConversation()).isEqualTo(conversation);
            assertThat(updated.getParticipant()).isEqualTo(participant);
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
