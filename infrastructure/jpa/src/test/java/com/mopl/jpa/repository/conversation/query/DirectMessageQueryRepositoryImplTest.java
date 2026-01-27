package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    DirectMessageQueryRepositoryImpl.class,
    DirectMessageEntityMapper.class,
    ConversationEntityMapper.class,
    UserEntityMapper.class
})
@DisplayName("DirectMessageQueryRepositoryImpl 슬라이스 테스트")
class DirectMessageQueryRepositoryImplTest {

    @Autowired
    private DirectMessageQueryRepository directMessageQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID conversationId;
    private UUID conversation2Id;
    private UUID conversation3Id;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        UserEntity user1 = createAndPersistUser("user1@example.com", "User1", baseTime);
        UserEntity user2 = createAndPersistUser("user2@example.com", "User2", baseTime);

        // 대화 1: 메시지 3개
        ConversationEntity conv1 = createAndPersistConversation(baseTime);
        conversationId = conv1.getId();
        createAndPersistDirectMessage(conv1, user1, "메시지 1", baseTime);
        createAndPersistDirectMessage(conv1, user2, "메시지 2", baseTime.plusSeconds(1));
        createAndPersistDirectMessage(conv1, user1, "메시지 3", baseTime.plusSeconds(2));

        // 대화 2: 메시지 2개
        ConversationEntity conv2 = createAndPersistConversation(baseTime.plusSeconds(10));
        conversation2Id = conv2.getId();
        createAndPersistDirectMessage(conv2, user1, "대화2 메시지 1", baseTime.plusSeconds(10));
        createAndPersistDirectMessage(conv2, user2, "대화2 메시지 2", baseTime.plusSeconds(11));

        // 대화 3: 메시지 없음
        ConversationEntity conv3 = createAndPersistConversation(baseTime.plusSeconds(20));
        conversation3Id = conv3.getId();

        entityManager.flush();
        entityManager.clear();
    }

    private UserEntity createAndPersistUser(String email, String name, Instant createdAt) {
        UserEntity entity = UserEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
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

    private ConversationEntity createAndPersistConversation(Instant createdAt) {
        ConversationEntity entity = ConversationEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private void createAndPersistDirectMessage(
        ConversationEntity conversation,
        UserEntity sender,
        String content,
        Instant createdAt
    ) {
        DirectMessageEntity entity = DirectMessageEntity.builder()
            .createdAt(createdAt)
            .conversation(conversation)
            .sender(sender)
            .content(content)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("conversationId로 해당 대화의 메시지만 조회")
        void withConversationId_returnsOnlyThatConversationMessages() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.totalCount()).isEqualTo(3);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("메시지가 없는 대화는 빈 결과 반환")
        void withNoMessages_returnsEmptyResult() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversation3Id, request
            );

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 대화는 빈 결과 반환")
        void withNonExistingConversation_returnsEmptyResult() {
            // given
            UUID nonExistingConversationId = UUID.randomUUID();
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                nonExistingConversationId, request
            );

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("findAll() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("생성일시로 내림차순 정렬")
        void sortByCreatedAtDescending() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);

            // 시간순 내림차순 확인
            for (int i = 0; i < response.data().size() - 1; i++) {
                assertThat(response.data().get(i).getCreatedAt())
                    .isAfterOrEqualTo(response.data().get(i + 1).getCreatedAt());
            }
        }

        @Test
        @DisplayName("생성일시로 오름차순 정렬")
        void sortByCreatedAtAscending() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.ASCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).hasSize(3);

            // 시간순 오름차순 확인
            for (int i = 0; i < response.data().size() - 1; i++) {
                assertThat(response.data().get(i).getCreatedAt())
                    .isBeforeOrEqualTo(response.data().get(i + 1).getCreatedAt());
            }
        }
    }

    @Nested
    @DisplayName("findAll() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 2, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            DirectMessageQueryRequest firstRequest = new DirectMessageQueryRequest(
                null, null, 2, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );
            CursorResponse<DirectMessageModel> firstResponse = directMessageQueryRepository.findAll(
                conversationId, firstRequest
            );

            DirectMessageQueryRequest secondRequest = new DirectMessageQueryRequest(
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> secondResponse = directMessageQueryRepository.findAll(
                conversationId, secondRequest
            );

            // then
            assertThat(secondResponse.data()).hasSize(1);
            assertThat(secondResponse.hasNext()).isFalse();
            assertThat(secondResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("오름차순 커서 페이지네이션")
        void ascendingPagination() {
            // given
            DirectMessageQueryRequest firstRequest = new DirectMessageQueryRequest(
                null, null, 2, SortDirection.ASCENDING, DirectMessageSortField.CREATED_AT
            );
            CursorResponse<DirectMessageModel> firstResponse = directMessageQueryRepository.findAll(
                conversationId, firstRequest
            );

            DirectMessageQueryRequest secondRequest = new DirectMessageQueryRequest(
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> secondResponse = directMessageQueryRepository.findAll(
                conversationId, secondRequest
            );

            // then
            assertThat(firstResponse.data()).hasSize(2);
            assertThat(secondResponse.data()).hasSize(1);

            // 첫 번째 페이지의 마지막 항목이 두 번째 페이지의 첫 항목보다 이전
            assertThat(firstResponse.data().getLast().getCreatedAt())
                .isBefore(secondResponse.data().getFirst().getCreatedAt());
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("기본값 적용 (sortDirection=DESCENDING, sortBy=createdAt)")
        void withNullValues_usesDefaults() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, null, null, null
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
        }
    }

    @Nested
    @DisplayName("findAll() - sender 포함 확인")
    class SenderInclusionTest {

        @Test
        @DisplayName("sender 정보가 포함되어 반환된다")
        void includesSenderInfo() {
            // given
            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conversationId, request
            );

            // then
            assertThat(response.data()).isNotEmpty();
            for (DirectMessageModel message : response.data()) {
                assertThat(message.getSender()).isNotNull();
                assertThat(message.getSender().getId()).isNotNull();
                assertThat(message.getSender().getName()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("findAll() - soft delete된 sender 처리")
    class SoftDeletedSenderTest {

        @Test
        @DisplayName("soft delete된 sender의 메시지는 조회되지 않는다")
        void withSoftDeletedSender_excludesMessage() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity deletedUser = createAndPersistUser(
                "deleted@example.com", "DeletedUser", baseTime
            );
            ConversationEntity conv = createAndPersistConversation(baseTime.plusSeconds(100));
            createAndPersistDirectMessage(conv, deletedUser, "삭제된 사용자 메시지", baseTime.plusSeconds(100));

            // soft delete user
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedUser.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conv.getId(), request
            );

            // then
            assertThat(response.data()).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 sender와 활성 sender가 섞인 대화에서 활성 sender 메시지만 조회된다")
        void withMixedSenders_returnsOnlyActiveMessages() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity activeUser = createAndPersistUser(
                "active@example.com", "ActiveUser", baseTime
            );
            UserEntity deletedUser = createAndPersistUser(
                "deleted2@example.com", "DeletedUser2", baseTime
            );
            ConversationEntity conv = createAndPersistConversation(baseTime.plusSeconds(200));
            createAndPersistDirectMessage(conv, activeUser, "활성 사용자 메시지", baseTime.plusSeconds(200));
            createAndPersistDirectMessage(conv, deletedUser, "삭제된 사용자 메시지", baseTime.plusSeconds(201));

            // soft delete user
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedUser.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            DirectMessageQueryRequest request = new DirectMessageQueryRequest(
                null, null, 100, SortDirection.DESCENDING, DirectMessageSortField.CREATED_AT
            );

            // when
            CursorResponse<DirectMessageModel> response = directMessageQueryRepository.findAll(
                conv.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getContent()).isEqualTo("활성 사용자 메시지");
            assertThat(response.data().getFirst().getSender()).isNotNull();
            assertThat(response.data().getFirst().getSender().getName()).isEqualTo("ActiveUser");
        }
    }

    @Nested
    @DisplayName("findLastDirectMessagesWithSenderByConversationIdIn()")
    class FindLastDirectMessagesTest {

        @Test
        @DisplayName("여러 대화의 마지막 메시지를 한 번에 조회")
        void withMultipleConversations_returnsLastMessages() {
            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(
                    List.of(conversationId, conversation2Id)
                );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(conversationId)).isNotNull();
            assertThat(result.get(conversationId).getContent()).isEqualTo("메시지 3");
            assertThat(result.get(conversation2Id)).isNotNull();
            assertThat(result.get(conversation2Id).getContent()).isEqualTo("대화2 메시지 2");
        }

        @Test
        @DisplayName("sender 정보가 포함되어 반환된다")
        void includesSenderInfo() {
            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(List.of(conversationId));

            // then
            DirectMessageModel lastMessage = result.get(conversationId);
            assertThat(lastMessage.getSender()).isNotNull();
            assertThat(lastMessage.getSender().getId()).isNotNull();
            assertThat(lastMessage.getSender().getName()).isNotNull();
        }

        @Test
        @DisplayName("메시지가 없는 대화는 결과에 포함되지 않음")
        void withNoMessages_notIncludedInResult() {
            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(
                    List.of(conversationId, conversation3Id)
                );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(conversationId)).isNotNull();
            assertThat(result.get(conversation3Id)).isNull();
        }

        @Test
        @DisplayName("빈 컬렉션은 빈 Map 반환")
        void withEmptyCollection_returnsEmptyMap() {
            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 대화 ID는 결과에 포함되지 않음")
        void withNonExistingConversation_notIncludedInResult() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(
                    List.of(conversationId, nonExistingId)
                );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(conversationId)).isNotNull();
            assertThat(result.get(nonExistingId)).isNull();
        }

        @Test
        @DisplayName("soft delete된 sender의 메시지는 조회되지 않는다")
        void withSoftDeletedSender_excludesMessage() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity deletedUser = createAndPersistUser(
                "deleted3@example.com", "DeletedUser3", baseTime
            );
            ConversationEntity conv = createAndPersistConversation(baseTime.plusSeconds(300));
            createAndPersistDirectMessage(conv, deletedUser, "마지막 메시지", baseTime.plusSeconds(300));

            // soft delete user
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedUser.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(List.of(conv.getId()));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 sender의 메시지가 마지막이면 그 이전 활성 sender 메시지가 마지막으로 조회된다")
        void withSoftDeletedLastSender_returnsPreviousActiveMessage() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity activeUser = createAndPersistUser(
                "active2@example.com", "ActiveUser2", baseTime
            );
            UserEntity deletedUser = createAndPersistUser(
                "deleted4@example.com", "DeletedUser4", baseTime
            );
            ConversationEntity conv = createAndPersistConversation(baseTime.plusSeconds(400));
            createAndPersistDirectMessage(conv, activeUser, "활성 사용자 메시지", baseTime.plusSeconds(400));
            createAndPersistDirectMessage(conv, deletedUser, "삭제된 사용자 마지막 메시지", baseTime.plusSeconds(401));

            // soft delete user
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedUser.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            // when
            Map<UUID, DirectMessageModel> result = directMessageQueryRepository
                .findLastDirectMessagesWithSenderByConversationIdIn(List.of(conv.getId()));

            // then
            assertThat(result).hasSize(1);
            DirectMessageModel lastMessage = result.get(conv.getId());
            assertThat(lastMessage).isNotNull();
            assertThat(lastMessage.getContent()).isEqualTo("활성 사용자 메시지");
            assertThat(lastMessage.getSender()).isNotNull();
            assertThat(lastMessage.getSender().getName()).isEqualTo("ActiveUser2");
        }
    }
}
