package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.fixture.ConversationModelFixture;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationService 단위 테스트")
class ConversationServiceTest {

    @Mock
    private ConversationQueryRepository conversationQueryRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            UUID userId = UUID.randomUUID();
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, null, null, null
            );
            CursorResponse<ConversationModel> expectedResponse = CursorResponse.empty(
                "CREATED_AT", SortDirection.DESCENDING
            );

            given(conversationQueryRepository.findAll(userId, request)).willReturn(expectedResponse);

            // when
            CursorResponse<ConversationModel> result = conversationService.getAll(userId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(conversationQueryRepository).should().findAll(userId, request);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 대화 ID로 조회하면 ConversationModel 반환")
        void withExistingId_returnsConversationModel() {
            // given
            ConversationModel conversation = ConversationModelFixture.create();
            UUID conversationId = conversation.getId();

            given(conversationRepository.findById(conversationId)).willReturn(Optional.of(conversation));

            // when
            ConversationModel result = conversationService.getById(conversationId);

            // then
            assertThat(result).isEqualTo(conversation);
            then(conversationRepository).should().findById(conversationId);
        }

        @Test
        @DisplayName("존재하지 않는 대화 ID로 조회하면 ConversationNotFoundException 발생")
        void withNonExistingId_throwsConversationNotFoundException() {
            // given
            UUID conversationId = UUID.randomUUID();

            given(conversationRepository.findById(conversationId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> conversationService.getById(conversationId))
                .isInstanceOf(ConversationNotFoundException.class)
                .satisfies(e -> {
                    ConversationNotFoundException ex = (ConversationNotFoundException) e;
                    assertThat(ex.getDetails().get("id")).isEqualTo(conversationId);
                });

            then(conversationRepository).should().findById(conversationId);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("대화 생성 성공")
        void createsConversation() {
            // given
            ConversationModel conversation = ConversationModel.create();

            given(conversationRepository.save(conversation)).willReturn(conversation);

            // when
            ConversationModel result = conversationService.create(conversation);

            // then
            assertThat(result).isEqualTo(conversation);
            then(conversationRepository).should().save(conversation);
        }
    }

    @Nested
    @DisplayName("getByParticipants()")
    class GetByParticipantsTest {

        @Test
        @DisplayName("존재하는 참여자들로 조회하면 ConversationModel 반환")
        void withExistingParticipants_returnsConversationModel() {
            // given
            UUID userId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();
            ConversationModel conversation = ConversationModelFixture.create();

            given(conversationQueryRepository.findByParticipants(userId, withUserId))
                .willReturn(Optional.of(conversation));

            // when
            ConversationModel result = conversationService.getByParticipants(userId, withUserId);

            // then
            assertThat(result).isEqualTo(conversation);
            then(conversationQueryRepository).should().findByParticipants(userId, withUserId);
        }

        @Test
        @DisplayName("존재하지 않는 참여자들로 조회하면 ConversationNotFoundException 발생")
        void withNonExistingParticipants_throwsConversationNotFoundException() {
            // given
            UUID userId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();

            given(conversationQueryRepository.findByParticipants(userId, withUserId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> conversationService.getByParticipants(userId, withUserId))
                .isInstanceOf(ConversationNotFoundException.class)
                .satisfies(e -> {
                    ConversationNotFoundException ex = (ConversationNotFoundException) e;
                    assertThat(ex.getDetails().get("requesterId")).isEqualTo(userId);
                    assertThat(ex.getDetails().get("withUserId")).isEqualTo(withUserId);
                });

            then(conversationQueryRepository).should().findByParticipants(userId, withUserId);
        }
    }

    @Nested
    @DisplayName("existsByParticipants()")
    class ExistsByParticipantsTest {

        @Test
        @DisplayName("대화가 존재하면 true 반환")
        void withExistingConversation_returnsTrue() {
            // given
            UUID userId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();

            given(conversationQueryRepository.existsByParticipants(userId, withUserId)).willReturn(true);

            // when
            boolean result = conversationService.existsByParticipants(userId, withUserId);

            // then
            assertThat(result).isTrue();
            then(conversationQueryRepository).should().existsByParticipants(userId, withUserId);
        }

        @Test
        @DisplayName("대화가 존재하지 않으면 false 반환")
        void withNonExistingConversation_returnsFalse() {
            // given
            UUID userId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();

            given(conversationQueryRepository.existsByParticipants(userId, withUserId)).willReturn(false);

            // when
            boolean result = conversationService.existsByParticipants(userId, withUserId);

            // then
            assertThat(result).isFalse();
            then(conversationQueryRepository).should().existsByParticipants(userId, withUserId);
        }
    }
}
