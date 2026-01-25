package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    ConversationRepositoryImpl.class,
    ConversationEntityMapper.class
})
@DisplayName("ConversationRepositoryImpl 슬라이스 테스트")
class ConversationRepositoryImplTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID existingConversationId;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        ConversationEntity conversation = ConversationEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .build();
        entityManager.persist(conversation);
        existingConversationId = conversation.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 대화 ID로 조회하면 반환")
        void withExistingId_returnsConversation() {
            // when
            Optional<ConversationModel> result = conversationRepository.findById(existingConversationId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(existingConversationId);
        }

        @Test
        @DisplayName("존재하지 않는 대화 ID로 조회하면 빈 Optional 반환")
        void withNonExistingId_returnsEmpty() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<ConversationModel> result = conversationRepository.findById(nonExistingId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 대화 저장 성공")
        void withNewConversation_savesSuccessfully() {
            // given
            ConversationModel newConversation = ConversationModel.create();

            // when
            ConversationModel savedConversation = conversationRepository.save(newConversation);

            // then
            assertThat(savedConversation.getId()).isNotNull();
            assertThat(savedConversation.getCreatedAt()).isNotNull();

            // DB에서 조회 확인
            entityManager.flush();
            entityManager.clear();
            Optional<ConversationModel> found = conversationRepository.findById(savedConversation.getId());
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("기존 대화 업데이트 성공")
        void withExistingConversation_updatesSuccessfully() {
            // given
            Optional<ConversationModel> existing = conversationRepository.findById(existingConversationId);
            assertThat(existing).isPresent();

            ConversationModel toUpdate = existing.get();

            // when
            ConversationModel updated = conversationRepository.save(toUpdate);

            // then
            assertThat(updated.getId()).isEqualTo(existingConversationId);
        }
    }
}
