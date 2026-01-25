package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.fixture.ReadStatusModelFixture;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadStatusService 단위 테스트")
class ReadStatusServiceTest {

    @Mock
    private ReadStatusRepository readStatusRepository;

    @InjectMocks
    private ReadStatusService readStatusService;

    @Nested
    @DisplayName("getReadStatusMap()")
    class GetReadStatusMapTest {

        @Test
        @DisplayName("빈 컬렉션이면 빈 Map 반환")
        void withEmptyCollection_returnsEmptyMap() {
            // given
            UUID participantId = UUID.randomUUID();
            List<UUID> conversationIds = List.of();

            // when
            Map<UUID, ReadStatusModel> result = readStatusService.getReadStatusMap(
                participantId, conversationIds
            );

            // then
            assertThat(result).isEmpty();
            then(readStatusRepository).should(never())
                .findByParticipantIdAndConversationIdIn(participantId, conversationIds);
        }

        @Test
        @DisplayName("Repository에 위임하여 Map 반환")
        void delegatesToRepository() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            List<UUID> conversationIds = List.of(conversationId);

            ReadStatusModel readStatus = ReadStatusModelFixture.builder()
                .set("conversation.id", conversationId)
                .sample();

            given(readStatusRepository.findByParticipantIdAndConversationIdIn(participantId, conversationIds))
                .willReturn(List.of(readStatus));

            // when
            Map<UUID, ReadStatusModel> result = readStatusService.getReadStatusMap(
                participantId, conversationIds
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(conversationId)).isEqualTo(readStatus);
            then(readStatusRepository).should()
                .findByParticipantIdAndConversationIdIn(participantId, conversationIds);
        }
    }

    @Nested
    @DisplayName("getOtherReadStatusMapWithParticipant()")
    class GetOtherReadStatusMapWithParticipantTest {

        @Test
        @DisplayName("빈 컬렉션이면 빈 Map 반환")
        void withEmptyCollection_returnsEmptyMap() {
            // given
            UUID participantId = UUID.randomUUID();
            List<UUID> conversationIds = List.of();

            // when
            Map<UUID, ReadStatusModel> result = readStatusService.getOtherReadStatusMapWithParticipant(
                participantId, conversationIds
            );

            // then
            assertThat(result).isEmpty();
            then(readStatusRepository).should(never())
                .findWithParticipantByParticipantIdNotAndConversationIdIn(participantId, conversationIds);
        }

        @Test
        @DisplayName("Repository에 위임하여 Map 반환")
        void delegatesToRepository() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            List<UUID> conversationIds = List.of(conversationId);

            ReadStatusModel readStatus = ReadStatusModelFixture.builder()
                .set("conversation.id", conversationId)
                .sample();

            given(readStatusRepository.findWithParticipantByParticipantIdNotAndConversationIdIn(
                participantId, conversationIds
            )).willReturn(List.of(readStatus));

            // when
            Map<UUID, ReadStatusModel> result = readStatusService.getOtherReadStatusMapWithParticipant(
                participantId, conversationIds
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(conversationId)).isEqualTo(readStatus);
            then(readStatusRepository).should()
                .findWithParticipantByParticipantIdNotAndConversationIdIn(participantId, conversationIds);
        }
    }

    @Nested
    @DisplayName("getReadStatus()")
    class GetReadStatusTest {

        @Test
        @DisplayName("존재하는 ReadStatus 반환")
        void withExistingReadStatus_returnsReadStatusModel() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            ReadStatusModel readStatus = ReadStatusModelFixture.create();

            given(readStatusRepository.findByParticipantIdAndConversationId(participantId, conversationId))
                .willReturn(Optional.of(readStatus));

            // when
            ReadStatusModel result = readStatusService.getReadStatus(participantId, conversationId);

            // then
            assertThat(result).isEqualTo(readStatus);
            then(readStatusRepository).should()
                .findByParticipantIdAndConversationId(participantId, conversationId);
        }

        @Test
        @DisplayName("존재하지 않으면 ReadStatusNotFoundException 발생")
        void withNonExistingReadStatus_throwsException() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            given(readStatusRepository.findByParticipantIdAndConversationId(participantId, conversationId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.getReadStatus(participantId, conversationId))
                .isInstanceOf(ReadStatusNotFoundException.class)
                .satisfies(e -> {
                    ReadStatusNotFoundException ex = (ReadStatusNotFoundException) e;
                    assertThat(ex.getDetails().get("participantId")).isEqualTo(participantId);
                    assertThat(ex.getDetails().get("conversationId")).isEqualTo(conversationId);
                });

            then(readStatusRepository).should()
                .findByParticipantIdAndConversationId(participantId, conversationId);
        }
    }

    @Nested
    @DisplayName("getReadStatusWithParticipant()")
    class GetReadStatusWithParticipantTest {

        @Test
        @DisplayName("존재하는 ReadStatus 반환")
        void withExistingReadStatus_returnsReadStatusModel() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            ReadStatusModel readStatus = ReadStatusModelFixture.create();

            given(readStatusRepository.findWithParticipantByParticipantIdAndConversationId(
                participantId, conversationId
            )).willReturn(Optional.of(readStatus));

            // when
            ReadStatusModel result = readStatusService.getReadStatusWithParticipant(
                participantId, conversationId
            );

            // then
            assertThat(result).isEqualTo(readStatus);
            then(readStatusRepository).should()
                .findWithParticipantByParticipantIdAndConversationId(participantId, conversationId);
        }

        @Test
        @DisplayName("존재하지 않으면 ReadStatusNotFoundException 발생")
        void withNonExistingReadStatus_throwsException() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            given(readStatusRepository.findWithParticipantByParticipantIdAndConversationId(
                participantId, conversationId
            )).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.getReadStatusWithParticipant(
                participantId, conversationId
            ))
                .isInstanceOf(ReadStatusNotFoundException.class)
                .satisfies(e -> {
                    ReadStatusNotFoundException ex = (ReadStatusNotFoundException) e;
                    assertThat(ex.getDetails().get("participantId")).isEqualTo(participantId);
                    assertThat(ex.getDetails().get("conversationId")).isEqualTo(conversationId);
                });

            then(readStatusRepository).should()
                .findWithParticipantByParticipantIdAndConversationId(participantId, conversationId);
        }
    }

    @Nested
    @DisplayName("getOtherReadStatusWithParticipant()")
    class GetOtherReadStatusWithParticipantTest {

        @Test
        @DisplayName("존재하는 ReadStatus 반환")
        void withExistingReadStatus_returnsReadStatusModel() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            ReadStatusModel readStatus = ReadStatusModelFixture.create();

            given(readStatusRepository.findWithParticipantByParticipantIdNotAndConversationId(
                participantId, conversationId
            )).willReturn(Optional.of(readStatus));

            // when
            ReadStatusModel result = readStatusService.getOtherReadStatusWithParticipant(
                participantId, conversationId
            );

            // then
            assertThat(result).isEqualTo(readStatus);
            then(readStatusRepository).should()
                .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId);
        }

        @Test
        @DisplayName("존재하지 않으면 null 반환")
        void withNonExistingReadStatus_returnsNull() {
            // given
            UUID participantId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();

            given(readStatusRepository.findWithParticipantByParticipantIdNotAndConversationId(
                participantId, conversationId
            )).willReturn(Optional.empty());

            // when
            ReadStatusModel result = readStatusService.getOtherReadStatusWithParticipant(
                participantId, conversationId
            );

            // then
            assertThat(result).isNull();
            then(readStatusRepository).should()
                .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("ReadStatus 생성 성공")
        void createsReadStatus() {
            // given
            ReadStatusModel readStatus = ReadStatusModelFixture.create();

            given(readStatusRepository.save(readStatus)).willReturn(readStatus);

            // when
            ReadStatusModel result = readStatusService.create(readStatus);

            // then
            assertThat(result).isEqualTo(readStatus);
            then(readStatusRepository).should().save(readStatus);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("ReadStatus 업데이트 성공")
        void updatesReadStatus() {
            // given
            ReadStatusModel readStatus = ReadStatusModelFixture.create();

            given(readStatusRepository.save(readStatus)).willReturn(readStatus);

            // when
            ReadStatusModel result = readStatusService.update(readStatus);

            // then
            assertThat(result).isEqualTo(readStatus);
            then(readStatusRepository).should().save(readStatus);
        }
    }
}
