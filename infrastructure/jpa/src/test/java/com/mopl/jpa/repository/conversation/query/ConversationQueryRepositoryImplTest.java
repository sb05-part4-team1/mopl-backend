package com.mopl.jpa.repository.conversation.query;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.user.UserEntity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    ConversationQueryRepositoryImpl.class,
    ConversationEntityMapper.class
})
@DisplayName("ConversationQueryRepositoryImpl 슬라이스 테스트")
class ConversationQueryRepositoryImplTest {

    @Autowired
    private ConversationQueryRepository conversationQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID userId;
    private UUID otherUser1Id;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        UserEntity user = createAndPersistUser("user@example.com", "User", baseTime);
        UserEntity otherUser1 = createAndPersistUser("alice@example.com", "Alice", baseTime);
        UserEntity otherUser2 = createAndPersistUser("bob@example.com", "Bob", baseTime);
        UserEntity otherUser3 = createAndPersistUser("charlie@example.com", "Charlie", baseTime);

        userId = user.getId();
        otherUser1Id = otherUser1.getId();

        // user와 otherUser1의 대화
        ConversationEntity conv1 = createAndPersistConversation(baseTime);
        createAndPersistReadStatus(conv1, user, baseTime);
        createAndPersistReadStatus(conv1, otherUser1, baseTime);

        // user와 otherUser2의 대화
        ConversationEntity conv2 = createAndPersistConversation(baseTime.plusSeconds(1));
        createAndPersistReadStatus(conv2, user, baseTime.plusSeconds(1));
        createAndPersistReadStatus(conv2, otherUser2, baseTime.plusSeconds(1));

        // user와 otherUser3의 대화
        ConversationEntity conv3 = createAndPersistConversation(baseTime.plusSeconds(2));
        createAndPersistReadStatus(conv3, user, baseTime.plusSeconds(2));
        createAndPersistReadStatus(conv3, otherUser3, baseTime.plusSeconds(2));

        // otherUser1와 otherUser2의 대화 (user 미참여)
        ConversationEntity conv4 = createAndPersistConversation(baseTime.plusSeconds(3));
        createAndPersistReadStatus(conv4, otherUser1, baseTime.plusSeconds(3));
        createAndPersistReadStatus(conv4, otherUser2, baseTime.plusSeconds(3));

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

    private void createAndPersistReadStatus(
        ConversationEntity conversation,
        UserEntity participant,
        Instant createdAt
    ) {
        ReadStatusEntity entity = ReadStatusEntity.builder()
            .createdAt(createdAt)
            .conversation(conversation)
            .participant(participant)
            .lastReadAt(createdAt)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAllConversation() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("userId로 참여한 대화만 조회")
        void withUserId_returnsOnlyParticipatingConversations() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.totalCount()).isEqualTo(3);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("다른 사용자의 대화는 조회되지 않음")
        void withOtherUserId_returnsOnlyTheirConversations() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(otherUser1Id, request);

            // then
            assertThat(response.data()).hasSize(2); // user와의 대화 + otherUser2와의 대화
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("keywordLike로 상대방 이름 필터링")
        void withKeywordLike_filtersConversationsByOtherUserName() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                "Ali", null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.totalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("keywordLike 대소문자 무시")
        void withKeywordLike_caseInsensitive() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                "bob", null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(1);
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 결과 반환")
        void withNoMatchingData_returnsEmptyResult() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                "nonexistent", null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("참여하지 않은 사용자는 빈 결과 반환")
        void withNonParticipatingUser_returnsEmptyResult() {
            // given
            UUID nonParticipatingUserId = UUID.randomUUID();
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(nonParticipatingUserId, request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("findAllConversation() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("생성일시로 내림차순 정렬")
        void sortByCreatedAtDescending() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 100, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.sortBy()).isEqualTo("createdAt");
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
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 100, SortDirection.ASCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

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
    @DisplayName("findAllConversation() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, 2, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

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
            ConversationQueryRequest firstRequest = new ConversationQueryRequest(
                null, null, null, 2, SortDirection.DESCENDING, ConversationSortField.createdAt
            );
            CursorResponse<ConversationModel> firstResponse = conversationQueryRepository.findAllConversation(userId, firstRequest);

            ConversationQueryRequest secondRequest = new ConversationQueryRequest(
                null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> secondResponse = conversationQueryRepository.findAllConversation(userId, secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(1);
            assertThat(secondResponse.hasNext()).isFalse();
            assertThat(secondResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("오름차순 커서 페이지네이션")
        void ascendingPagination() {
            // given
            ConversationQueryRequest firstRequest = new ConversationQueryRequest(
                null, null, null, 2, SortDirection.ASCENDING, ConversationSortField.createdAt
            );
            CursorResponse<ConversationModel> firstResponse = conversationQueryRepository.findAllConversation(userId, firstRequest);

            ConversationQueryRequest secondRequest = new ConversationQueryRequest(
                null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> secondResponse = conversationQueryRepository.findAllConversation(userId, secondRequest);

            // then
            assertThat(firstResponse.data()).hasSize(2);
            assertThat(secondResponse.data()).hasSize(1);

            // 첫 번째 페이지의 마지막 항목이 두 번째 페이지의 첫 항목보다 이전
            assertThat(firstResponse.data().getLast().getCreatedAt())
                .isBefore(secondResponse.data().getFirst().getCreatedAt());
        }

        @Test
        @DisplayName("필터와 페이지네이션 조합")
        void paginationWithFilter() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                "li", null, null, 1, SortDirection.DESCENDING, ConversationSortField.createdAt
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.totalCount()).isEqualTo(2); // Alice, Charlie
            assertThat(response.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("findAllConversation() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("기본값 적용 (sortDirection=DESCENDING, sortBy=createdAt)")
        void withNullValues_usesDefaults() {
            // given
            ConversationQueryRequest request = new ConversationQueryRequest(
                null, null, null, null, null, null
            );

            // when
            CursorResponse<ConversationModel> response = conversationQueryRepository.findAllConversation(userId, request);

            // then
            assertThat(response.data()).hasSize(3);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
            assertThat(response.sortBy()).isEqualTo("createdAt");
        }
    }
}
