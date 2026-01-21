package com.mopl.jpa.repository.notification.query;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.model.user.UserModel.Role;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.repository.notification.NotificationSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
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
    NotificationQueryRepositoryImpl.class,
    NotificationEntityMapper.class
})
@DisplayName("NotificationQueryRepositoryImpl 슬라이스 테스트")
class NotificationQueryRepositoryImplTest {

    @Autowired
    private NotificationQueryRepository notificationQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        user1 = createAndPersistUser("user1@example.com", "User1");
        user2 = createAndPersistUser("user2@example.com", "User2");

        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        // user1의 알림 5개 생성
        createAndPersistNotification("알림1", "내용1", NotificationModel.NotificationLevel.INFO, user1, baseTime);
        createAndPersistNotification("알림2", "내용2", NotificationModel.NotificationLevel.WARNING, user1, baseTime
            .plusSeconds(1));
        createAndPersistNotification("알림3", "내용3", NotificationModel.NotificationLevel.ERROR, user1, baseTime
            .plusSeconds(2));
        createAndPersistNotification("알림4", "내용4", NotificationModel.NotificationLevel.INFO, user1, baseTime
            .plusSeconds(3));
        createAndPersistNotification("알림5", "내용5", NotificationModel.NotificationLevel.WARNING, user1, baseTime
            .plusSeconds(4));

        // user2의 알림 2개 생성
        createAndPersistNotification("다른알림1", "다른내용1", NotificationModel.NotificationLevel.INFO, user2, baseTime
            .plusSeconds(5));
        createAndPersistNotification("다른알림2", "다른내용2", NotificationModel.NotificationLevel.ERROR, user2, baseTime
            .plusSeconds(6));

        entityManager.flush();
        entityManager.clear();
    }

    private UserEntity createAndPersistUser(String email, String name) {
        UserEntity entity = UserEntity.builder()
            .authProvider(AuthProvider.EMAIL)
            .email(email)
            .name(name)
            .password("encodedPassword")
            .role(Role.USER)
            .locked(false)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private void createAndPersistNotification(
        String title,
        String content,
        NotificationModel.NotificationLevel level,
        UserEntity receiver,
        Instant createdAt
    ) {
        NotificationEntity entity = NotificationEntity.builder()
            .createdAt(createdAt)
            .title(title)
            .content(content)
            .level(level)
            .receiverId(receiver.getId())
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("특정 사용자의 알림만 조회")
        void withReceiverId_filtersNotifications() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.totalCount()).isEqualTo(5);
            assertThat(response.data())
                .allMatch(n -> n.getReceiverId().equals(user1.getId()));
        }

        @Test
        @DisplayName("다른 사용자의 알림 조회")
        void withOtherReceiverId_returnsOtherUserNotifications() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user2.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.data())
                .allMatch(n -> n.getReceiverId().equals(user2.getId()));
        }

        @Test
        @DisplayName("알림이 없는 사용자 조회 시 빈 결과 반환")
        void withNoNotifications_returnsEmptyResult() {
            // given
            UUID nonExistentUserId = UUID.randomUUID();
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                nonExistentUserId, request
            );

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("삭제된 알림은 조회되지 않음")
        void withDeletedNotification_excludesFromQuery() {
            // given
            Instant now = Instant.now();
            NotificationEntity deletedNotification = NotificationEntity.builder()
                .createdAt(now)
                .title("삭제된 알림")
                .content("삭제된 내용")
                .level(NotificationModel.NotificationLevel.INFO)
                .receiverId(user1.getId())
                .deletedAt(now)
                .build();
            entityManager.persist(deletedNotification);
            entityManager.flush();
            entityManager.clear();

            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(5); // 삭제된 알림 제외
            assertThat(response.data())
                .noneMatch(n -> n.getTitle().equals("삭제된 알림"));
        }
    }

    @Nested
    @DisplayName("findAll() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("생성일시로 오름차순 정렬")
        void sortByCreatedAtAscending() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림1", "알림2", "알림3", "알림4", "알림5");
            assertThat(response.sortBy()).isEqualTo("createdAt");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("생성일시로 내림차순 정렬")
        void sortByCreatedAtDescending() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 100, SortDirection.DESCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림5", "알림4", "알림3", "알림2", "알림1");
        }
    }

    @Nested
    @DisplayName("findAll() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, 2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림1", "알림2");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            NotificationQueryRequest firstRequest = new NotificationQueryRequest(
                null, null, 2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );
            CursorResponse<NotificationModel> firstResponse = notificationQueryRepository.findAll(
                user1.getId(), firstRequest
            );

            NotificationQueryRequest secondRequest = new NotificationQueryRequest(
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> secondResponse = notificationQueryRepository.findAll(
                user1.getId(), secondRequest
            );

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림3", "알림4");
            assertThat(secondResponse.hasNext()).isTrue();
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext=false")
        void lastPage_hasNextIsFalse() {
            // given
            NotificationQueryRequest firstRequest = new NotificationQueryRequest(
                null, null, 2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );
            CursorResponse<NotificationModel> firstResponse = notificationQueryRepository.findAll(
                user1.getId(), firstRequest
            );

            NotificationQueryRequest secondRequest = new NotificationQueryRequest(
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );
            CursorResponse<NotificationModel> secondResponse = notificationQueryRepository.findAll(
                user1.getId(), secondRequest
            );

            NotificationQueryRequest thirdRequest = new NotificationQueryRequest(
                secondResponse.nextCursor(),
                secondResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> thirdResponse = notificationQueryRepository.findAll(
                user1.getId(), thirdRequest
            );

            // then
            assertThat(thirdResponse.data()).hasSize(1);
            assertThat(thirdResponse.data().get(0).getTitle()).isEqualTo("알림5");
            assertThat(thirdResponse.hasNext()).isFalse();
            assertThat(thirdResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("내림차순 커서 페이지네이션")
        void descendingPagination() {
            // given
            NotificationQueryRequest firstRequest = new NotificationQueryRequest(
                null, null, 2, SortDirection.DESCENDING, NotificationSortField.createdAt
            );
            CursorResponse<NotificationModel> firstResponse = notificationQueryRepository.findAll(
                user1.getId(), firstRequest
            );

            NotificationQueryRequest secondRequest = new NotificationQueryRequest(
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, NotificationSortField.createdAt
            );

            // when
            CursorResponse<NotificationModel> secondResponse = notificationQueryRepository.findAll(
                user1.getId(), secondRequest
            );

            // then
            assertThat(firstResponse.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림5", "알림4");
            assertThat(secondResponse.data())
                .extracting(NotificationModel::getTitle)
                .containsExactly("알림3", "알림2");
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("limit이 null이면 기본값 100 적용")
        void withNullLimit_usesDefaultLimit() {
            // given
            NotificationQueryRequest request = new NotificationQueryRequest(
                null, null, null, null, null
            );

            // when
            CursorResponse<NotificationModel> response = notificationQueryRepository.findAll(
                user1.getId(), request
            );

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
            assertThat(response.sortBy()).isEqualTo("createdAt");
        }
    }
}
