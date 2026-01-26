package com.mopl.jpa.repository.review.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    ReviewQueryRepositoryImpl.class,
    ReviewEntityMapper.class,
    UserEntityMapper.class,
    ContentEntityMapper.class
})
@DisplayName("ReviewQueryRepositoryImpl 슬라이스 테스트")
class ReviewQueryRepositoryImplTest {

    @Autowired
    private ReviewQueryRepository reviewQueryRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID contentId1;
    private UUID contentId2;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        UserEntity author1 = createAndPersistUser("author1@example.com", "Author1", baseTime);
        UserEntity author2 = createAndPersistUser("author2@example.com", "Author2", baseTime);
        UserEntity author3 = createAndPersistUser("author3@example.com", "Author3", baseTime);
        UserEntity author4 = createAndPersistUser("author4@example.com", "Author4", baseTime);

        ContentEntity content1 = createAndPersistContent("Content 1", baseTime);
        ContentEntity content2 = createAndPersistContent("Content 2", baseTime);
        contentId1 = content1.getId();
        contentId2 = content2.getId();

        // content1에 리뷰 4개 (각각 다른 author)
        createAndPersistReview(content1, author1, "리뷰 1", 5.0, baseTime);
        createAndPersistReview(content1, author2, "리뷰 2", 4.0, baseTime.plusSeconds(1));
        createAndPersistReview(content1, author3, "리뷰 3", 3.0, baseTime.plusSeconds(2));
        createAndPersistReview(content1, author4, "리뷰 4", 4.5, baseTime.plusSeconds(3));

        // content2에 리뷰 2개
        createAndPersistReview(content2, author1, "리뷰 A", 5.0, baseTime.plusSeconds(4));
        createAndPersistReview(content2, author2, "리뷰 B", 2.0, baseTime.plusSeconds(5));

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

    private ContentEntity createAndPersistContent(String title, Instant createdAt) {
        ContentEntity entity = ContentEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .type(ContentModel.ContentType.movie)
            .title(title)
            .description("Description")
            .thumbnailPath("contents/test-thumbnail.png")
            .averageRating(0.0)
            .reviewCount(0)
            .build();
        entityManager.persist(entity);
        return entity;
    }

    private void createAndPersistReview(
        ContentEntity content,
        UserEntity author,
        String text,
        double rating,
        Instant createdAt
    ) {
        ReviewEntity entity = ReviewEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .content(content)
            .author(author)
            .text(text)
            .rating(rating)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void withNoFilter_returnsAllReviews() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                null, null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(6);
            assertThat(response.totalCount()).isEqualTo(6);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("contentId로 필터링")
        void withContentId_filtersReviews() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(4);
            assertThat(response.data())
                .allMatch(review -> review.getContent().getId().equals(contentId1));
            assertThat(response.totalCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("다른 contentId로 필터링")
        void withOtherContentId_filtersReviews() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId2, null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(review -> review.getContent().getId().equals(contentId2));
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 결과 반환")
        void withNoMatchingData_returnsEmptyResult() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                UUID.randomUUID(), null, null, 100, SortDirection.DESCENDING,
                ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("생성일시로 내림차순 정렬")
        void sortByCreatedAtDescending() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 4", "리뷰 3", "리뷰 2", "리뷰 1");
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("생성일시로 오름차순 정렬")
        void sortByCreatedAtAscending() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 100, SortDirection.ASCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 1", "리뷰 2", "리뷰 3", "리뷰 4");
        }

        @Test
        @DisplayName("평점으로 내림차순 정렬")
        void sortByRatingDescending() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 100, SortDirection.DESCENDING, ReviewSortField.RATING
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ReviewModel::getRating)
                .containsExactly(5.0, 4.5, 4.0, 3.0);
        }

        @Test
        @DisplayName("평점으로 오름차순 정렬")
        void sortByRatingAscending() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 100, SortDirection.ASCENDING, ReviewSortField.RATING
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ReviewModel::getRating)
                .containsExactly(3.0, 4.0, 4.5, 5.0);
        }
    }

    @Nested
    @DisplayName("findAll() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                contentId1, null, null, 2, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 4", "리뷰 3");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            ReviewQueryRequest firstRequest = new ReviewQueryRequest(
                contentId1, null, null, 2, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );
            CursorResponse<ReviewModel> firstResponse = reviewQueryRepository.findAll(firstRequest);

            ReviewQueryRequest secondRequest = new ReviewQueryRequest(
                contentId1,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> secondResponse = reviewQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 2", "리뷰 1");
            assertThat(secondResponse.hasNext()).isFalse();
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext=false")
        void lastPage_hasNextIsFalse() {
            // given
            ReviewQueryRequest firstRequest = new ReviewQueryRequest(
                contentId1, null, null, 3, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );
            CursorResponse<ReviewModel> firstResponse = reviewQueryRepository.findAll(firstRequest);

            ReviewQueryRequest secondRequest = new ReviewQueryRequest(
                contentId1,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                3, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> secondResponse = reviewQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(1);
            assertThat(secondResponse.data().getFirst().getText()).isEqualTo("리뷰 1");
            assertThat(secondResponse.hasNext()).isFalse();
            assertThat(secondResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("오름차순 커서 페이지네이션")
        void ascendingPagination() {
            // given
            ReviewQueryRequest firstRequest = new ReviewQueryRequest(
                contentId1, null, null, 2, SortDirection.ASCENDING, ReviewSortField.CREATED_AT
            );
            CursorResponse<ReviewModel> firstResponse = reviewQueryRepository.findAll(firstRequest);

            ReviewQueryRequest secondRequest = new ReviewQueryRequest(
                contentId1,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> secondResponse = reviewQueryRepository.findAll(
                secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 1", "리뷰 2");
            assertThat(secondResponse.data())
                .extracting(ReviewModel::getText)
                .containsExactly("리뷰 3", "리뷰 4");
        }

        @Test
        @DisplayName("평점으로 커서 페이지네이션")
        void paginationByRating() {
            // given
            ReviewQueryRequest firstRequest = new ReviewQueryRequest(
                contentId1, null, null, 2, SortDirection.DESCENDING, ReviewSortField.RATING
            );
            CursorResponse<ReviewModel> firstResponse = reviewQueryRepository.findAll(firstRequest);

            ReviewQueryRequest secondRequest = new ReviewQueryRequest(
                contentId1,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, ReviewSortField.RATING
            );

            // when
            CursorResponse<ReviewModel> secondResponse = reviewQueryRepository.findAll(
                secondRequest
            );

            // then
            assertThat(firstResponse.data())
                .extracting(ReviewModel::getRating)
                .containsExactly(5.0, 4.5);
            assertThat(secondResponse.data())
                .extracting(ReviewModel::getRating)
                .containsExactly(4.0, 3.0);
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("기본값 적용 (sortDirection=DESCENDING, sortBy=createdAt)")
        void withNullValues_usesDefaults() {
            // given
            ReviewQueryRequest request = new ReviewQueryRequest(
                null, null, null, null, null, null
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(6);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
        }
    }

    @Nested
    @DisplayName("findAll() - soft delete된 author 필터링")
    class SoftDeletedAuthorTest {

        @Test
        @DisplayName("soft delete된 author의 리뷰는 조회되지 않는다")
        void withSoftDeletedAuthor_excludesReviews() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity deletedAuthor = createAndPersistUser(
                "deleted@example.com", "DeletedAuthor", baseTime
            );
            ContentEntity content = createAndPersistContent("Test Content", baseTime.plusSeconds(100));
            createAndPersistReview(content, deletedAuthor, "삭제된 작성자 리뷰", 5.0, baseTime.plusSeconds(100));

            // soft delete author
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedAuthor.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            ReviewQueryRequest request = new ReviewQueryRequest(
                content.getId(), null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }

        @Test
        @DisplayName("soft delete된 author와 활성 author가 섞인 콘텐츠에서 활성 author의 리뷰만 조회된다")
        void withMixedAuthors_returnsOnlyActiveAuthorReviews() {
            // given
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            UserEntity activeAuthor = createAndPersistUser(
                "active@example.com", "ActiveAuthor", baseTime
            );
            UserEntity deletedAuthor = createAndPersistUser(
                "deleted2@example.com", "DeletedAuthor2", baseTime
            );
            ContentEntity content = createAndPersistContent("Mixed Content", baseTime.plusSeconds(200));
            createAndPersistReview(content, activeAuthor, "활성 작성자 리뷰", 4.0, baseTime.plusSeconds(200));
            createAndPersistReview(content, deletedAuthor, "삭제된 작성자 리뷰", 5.0, baseTime.plusSeconds(201));

            // soft delete author
            entityManager.createQuery("UPDATE UserEntity u SET u.deletedAt = :now WHERE u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", deletedAuthor.getId())
                .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            ReviewQueryRequest request = new ReviewQueryRequest(
                content.getId(), null, null, 100, SortDirection.DESCENDING, ReviewSortField.CREATED_AT
            );

            // when
            CursorResponse<ReviewModel> response = reviewQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getText()).isEqualTo("활성 작성자 리뷰");
            assertThat(response.totalCount()).isEqualTo(1);
        }
    }
}
