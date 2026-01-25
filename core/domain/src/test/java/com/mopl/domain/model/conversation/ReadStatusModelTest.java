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
    @DisplayName("updateLastReadAt()")
    class UpdateLastReadAtTest {

        @Test
        @DisplayName("readAt이 lastReadAt 이후이면 lastReadAt이 갱신된다")
        void whenReadAtIsAfterLastReadAt_updatesLastReadAt() {
            // given
            Instant pastTime = Instant.now().minusSeconds(3600);
            Instant newReadAt = Instant.now();
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(pastTime)
                .build();

            // when
            ReadStatusModel updated = readStatus.updateLastReadAt(newReadAt);

            // then
            assertThat(updated.getLastReadAt()).isEqualTo(newReadAt);
            assertThat(updated).isNotSameAs(readStatus);
        }

        @Test
        @DisplayName("readAt이 lastReadAt 이전이면 동일 객체를 반환한다")
        void whenReadAtIsBeforeLastReadAt_returnsSameInstance() {
            // given
            Instant currentReadAt = Instant.now();
            Instant olderReadAt = currentReadAt.minusSeconds(3600);
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(currentReadAt)
                .build();

            // when
            ReadStatusModel updated = readStatus.updateLastReadAt(olderReadAt);

            // then
            assertThat(updated).isSameAs(readStatus);
            assertThat(updated.getLastReadAt()).isEqualTo(currentReadAt);
        }

        @Test
        @DisplayName("readAt이 lastReadAt과 같으면 동일 객체를 반환한다")
        void whenReadAtEqualsLastReadAt_returnsSameInstance() {
            // given
            Instant sameTime = Instant.now();
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(sameTime)
                .build();

            // when
            ReadStatusModel updated = readStatus.updateLastReadAt(sameTime);

            // then
            assertThat(updated).isSameAs(readStatus);
        }

        @Test
        @DisplayName("lastReadAt이 null이면 갱신된다")
        void whenLastReadAtIsNull_updatesLastReadAt() {
            // given
            Instant newReadAt = Instant.now();
            ReadStatusModel readStatus = ReadStatusModel.builder()
                .conversation(mock(ConversationModel.class))
                .participant(mock(UserModel.class))
                .lastReadAt(null)
                .build();

            // when
            ReadStatusModel updated = readStatus.updateLastReadAt(newReadAt);

            // then
            assertThat(updated.getLastReadAt()).isEqualTo(newReadAt);
            assertThat(updated).isNotSameAs(readStatus);
        }

        @Test
        @DisplayName("updateLastReadAt 호출 시 다른 필드는 유지된다")
        void updateLastReadAt_preservesOtherFields() {
            // given
            ConversationModel conversation = mock(ConversationModel.class);
            UserModel participant = mock(UserModel.class);
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now().minusSeconds(7200);
            Instant newReadAt = Instant.now();

            ReadStatusModel readStatus = ReadStatusModel.builder()
                .id(id)
                .conversation(conversation)
                .participant(participant)
                .lastReadAt(Instant.now().minusSeconds(3600))
                .createdAt(createdAt)
                .build();

            // when
            ReadStatusModel updated = readStatus.updateLastReadAt(newReadAt);

            // then
            assertThat(updated.getId()).isEqualTo(id);
            assertThat(updated.getConversation()).isEqualTo(conversation);
            assertThat(updated.getParticipant()).isEqualTo(participant);
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
