package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    ReadStatusRepositoryImpl.class,
    ReadStatusEntityMapper.class,
    ConversationEntityMapper.class,
    UserEntityMapper.class
})
@DisplayName("ReadStatusRepositoryImpl 슬라이스 테스트")
class ReadStatusRepositoryImplTest {

    @Autowired
    private ReadStatusRepository readStatusRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID user1Id;
    private UUID user2Id;
    private UUID conversation1Id;
    private UUID conversation2Id;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        UserEntity user1 = createAndPersistUser("user1@example.com", "User1");
        UserEntity user2 = createAndPersistUser("user2@example.com", "User2");
        user1Id = user1.getId();
        user2Id = user2.getId();

        ConversationEntity conversation1 = createAndPersistConversation();
        ConversationEntity conversation2 = createAndPersistConversation();
        conversation1Id = conversation1.getId();
        conversation2Id = conversation2.getId();

        // conversation1: user1, user2 참여
        createAndPersistReadStatus(conversation1, user1, baseTime);
        createAndPersistReadStatus(conversation1, user2, baseTime.plusSeconds(10));

        // conversation2: user1만 참여 (테스트용)
        createAndPersistReadStatus(conversation2, user1, baseTime);

        entityManager.flush();
        entityManager.clear();
    }

    private UserEntity createAndPersistUser(String email, String name) {
        UserEntity entity = UserEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email(email)
            .name(name)
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private ConversationEntity createAndPersistConversation() {
        ConversationEntity entity = ConversationEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private void createAndPersistReadStatus(ConversationEntity conversation, UserEntity participant, Instant lastReadAt) {
        ReadStatusEntity entity = ReadStatusEntity.builder()
            .createdAt(baseTime)
            .conversation(conversation)
            .participant(participant)
            .lastReadAt(lastReadAt)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findByParticipantIdAndConversationIdIn()")
    class FindByParticipantIdAndConversationIdInTest {

        @Test
        @DisplayName("참여자의 여러 대화 읽음 상태 조회")
        void withMultipleConversations_returnsReadStatuses() {
            // when
            List<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationIdIn(
                user1Id, List.of(conversation1Id, conversation2Id)
            );

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("빈 대화 목록은 빈 결과 반환")
        void withEmptyConversationIds_returnsEmpty() {
            // when
            List<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationIdIn(
                user1Id, List.of()
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("참여하지 않은 대화는 결과에 포함되지 않음")
        void withNonParticipatingConversation_notIncluded() {
            // when
            List<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationIdIn(
                user2Id, List.of(conversation1Id, conversation2Id)
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getConversation().getId()).isEqualTo(conversation1Id);
        }
    }

    @Nested
    @DisplayName("findWithParticipantByParticipantIdNotAndConversationIdIn()")
    class FindWithParticipantByParticipantIdNotAndConversationIdInTest {

        @Test
        @DisplayName("다른 참여자의 읽음 상태와 참여자 정보 조회")
        void withConversations_returnsOtherParticipantsWithInfo() {
            // when
            List<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdNotAndConversationIdIn(
                user1Id, List.of(conversation1Id)
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getParticipant()).isNotNull();
            assertThat(result.getFirst().getParticipant().getId()).isEqualTo(user2Id);
            assertThat(result.getFirst().getParticipant().getName()).isEqualTo("User2");
        }

        @Test
        @DisplayName("빈 대화 목록은 빈 결과 반환")
        void withEmptyConversationIds_returnsEmpty() {
            // when
            List<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdNotAndConversationIdIn(
                user1Id, List.of()
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByParticipantIdAndConversationId()")
    class FindByParticipantIdAndConversationIdTest {

        @Test
        @DisplayName("참여자의 특정 대화 읽음 상태 조회")
        void withExistingStatus_returnsReadStatus() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationId(
                user1Id, conversation1Id
            );

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getLastReadAt()).isEqualTo(baseTime);
        }

        @Test
        @DisplayName("참여하지 않은 대화는 빈 Optional 반환")
        void withNonParticipating_returnsEmpty() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationId(
                user2Id, conversation2Id
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 대화는 빈 Optional 반환")
        void withNonExistingConversation_returnsEmpty() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findByParticipantIdAndConversationId(
                user1Id, UUID.randomUUID()
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findWithParticipantByParticipantIdAndConversationId()")
    class FindWithParticipantByParticipantIdAndConversationIdTest {

        @Test
        @DisplayName("참여자 정보와 함께 읽음 상태 조회")
        void withExistingStatus_returnsReadStatusWithParticipant() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdAndConversationId(
                user1Id, conversation1Id
            );

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getParticipant()).isNotNull();
            assertThat(result.get().getParticipant().getId()).isEqualTo(user1Id);
            assertThat(result.get().getParticipant().getName()).isEqualTo("User1");
        }

        @Test
        @DisplayName("참여하지 않은 대화는 빈 Optional 반환")
        void withNonParticipating_returnsEmpty() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdAndConversationId(
                user2Id, conversation2Id
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findWithParticipantByParticipantIdNotAndConversationId()")
    class FindWithParticipantByParticipantIdNotAndConversationIdTest {

        @Test
        @DisplayName("다른 참여자의 읽음 상태와 정보 조회")
        void withOtherParticipant_returnsOtherReadStatus() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdNotAndConversationId(
                user1Id, conversation1Id
            );

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getParticipant()).isNotNull();
            assertThat(result.get().getParticipant().getId()).isEqualTo(user2Id);
            assertThat(result.get().getParticipant().getName()).isEqualTo("User2");
        }

        @Test
        @DisplayName("다른 참여자가 없으면 빈 Optional 반환")
        void withNoOtherParticipant_returnsEmpty() {
            // when
            Optional<ReadStatusModel> result = readStatusRepository.findWithParticipantByParticipantIdNotAndConversationId(
                user1Id, conversation2Id
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 읽음 상태 저장 성공")
        void withNewReadStatus_savesSuccessfully() {
            // given
            UserEntity newUser = createAndPersistUser("newuser@example.com", "NewUser");
            entityManager.flush();
            entityManager.clear();

            ReadStatusModel newReadStatus = ReadStatusModel.builder()
                .conversation(ConversationModel.builder().id(conversation1Id).build())
                .participant(UserModel.builder().id(newUser.getId()).build())
                .lastReadAt(baseTime)
                .build();

            // when
            ReadStatusModel saved = readStatusRepository.save(newReadStatus);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getLastReadAt()).isEqualTo(baseTime);

            // DB에서 조회 확인
            entityManager.flush();
            entityManager.clear();
            Optional<ReadStatusModel> found = readStatusRepository.findByParticipantIdAndConversationId(
                newUser.getId(), conversation1Id
            );
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("기존 읽음 상태 업데이트 성공")
        void withExistingReadStatus_updatesSuccessfully() {
            // given
            Optional<ReadStatusModel> existing = readStatusRepository.findByParticipantIdAndConversationId(
                user1Id, conversation1Id
            );
            assertThat(existing).isPresent();

            Instant newLastReadAt = baseTime.plusSeconds(100);
            ReadStatusModel toUpdate = ReadStatusModel.builder()
                .id(existing.get().getId())
                .createdAt(existing.get().getCreatedAt())
                .conversation(ConversationModel.builder().id(conversation1Id).build())
                .participant(UserModel.builder().id(user1Id).build())
                .lastReadAt(newLastReadAt)
                .build();

            // when
            ReadStatusModel updated = readStatusRepository.save(toUpdate);

            // then
            assertThat(updated.getLastReadAt()).isEqualTo(newLastReadAt);

            // DB에서 조회 확인
            entityManager.flush();
            entityManager.clear();
            Optional<ReadStatusModel> found = readStatusRepository.findByParticipantIdAndConversationId(
                user1Id, conversation1Id
            );
            assertThat(found).isPresent();
            assertThat(found.get().getLastReadAt()).isEqualTo(newLastReadAt);
        }
    }
}
