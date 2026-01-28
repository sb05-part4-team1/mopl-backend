package com.mopl.jpa.repository.content.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.content.query.ContentQueryRepository;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    ContentQueryRepositoryImpl.class,
    ContentEntityMapper.class
})
@DisplayName("ContentQueryRepositoryImpl 슬라이스 테스트")
class ContentQueryRepositoryImplTest {

    @Autowired
    private ContentQueryRepository contentQueryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        createAndPersistContent("인셉션", "꿈속의 꿈을 다룬 SF 영화", ContentType.movie, 100, 4.5, 100.0, baseTime);
        createAndPersistContent("다크나이트", "배트맨 시리즈 중 최고작", ContentType.movie, 200, 4.8, 200.0, baseTime.plusSeconds(1));
        createAndPersistContent("브레이킹 배드", "화학 선생님의 마약 제조 이야기", ContentType.tvSeries, 150, 4.9, 150.0, baseTime.plusSeconds(2));
        createAndPersistContent("왕좌의 게임", "판타지 드라마의 정점", ContentType.tvSeries, 180, 4.3, 180.0, baseTime.plusSeconds(3));
        createAndPersistContent("EPL 경기", "프리미어리그 축구 중계", ContentType.sport, 50, 3.5, 50.0, baseTime.plusSeconds(4));

        entityManager.flush();
        entityManager.clear();
    }

    private void createAndPersistContent(
        String title,
        String description,
        ContentType type,
        int reviewCount,
        double averageRating,
        double popularityScore,
        Instant createdAt
    ) {
        ContentEntity entity = ContentEntity.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .type(type)
            .title(title)
            .description(description)
            .thumbnailPath("contents/" + title + ".png")
            .reviewCount(reviewCount)
            .averageRating(averageRating)
            .popularityScore(popularityScore)
            .build();
        entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findAll() - 필터링")
    class FilteringTest {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void withNoFilter_returnsAllContents() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.totalCount()).isEqualTo(5);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("typeEqual로 필터링")
        void withTypeEqual_filtersContents() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                ContentType.movie, null, null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .allMatch(content -> content.getType() == ContentType.movie);
            assertThat(response.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("keywordLike로 제목 필터링")
        void withKeywordLike_filtersByTitle() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, "배트맨", null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getTitle()).isEqualTo("다크나이트");
        }

        @Test
        @DisplayName("keywordLike로 설명 필터링")
        void withKeywordLike_filtersByDescription() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, "드라마", null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getTitle()).isEqualTo("왕좌의 게임");
        }

        @Test
        @DisplayName("복합 필터 조합")
        void withMultipleFilters_filtersContents() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                ContentType.tvSeries, "드라마", null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().getFirst().getTitle()).isEqualTo("왕좌의 게임");
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 결과 반환")
        void withNoMatchingData_returnsEmptyResult() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, "존재하지않는키워드", null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.totalCount()).isZero();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 키워드로 필터링하면 전체 조회")
        void withEmptyKeyword_returnsAll() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, "", null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("findAll() - 정렬")
    class SortingTest {

        @Test
        @DisplayName("생성일시로 오름차순 정렬")
        void sortByCreatedAtAscending() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 100, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("인셉션", "다크나이트", "브레이킹 배드", "왕좌의 게임", "EPL 경기");
            assertThat(response.sortBy()).isEqualTo("CREATED_AT");
            assertThat(response.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("생성일시로 내림차순 정렬")
        void sortByCreatedAtDescending() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("EPL 경기", "왕좌의 게임", "브레이킹 배드", "다크나이트", "인셉션");
        }

        @Test
        @DisplayName("시청자 수(reviewCount)로 정렬")
        void sortByWatcherCount() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ContentModel::getReviewCount)
                .containsExactly(200, 180, 150, 100, 50);
        }

        @Test
        @DisplayName("평점으로 정렬")
        void sortByRate() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 100, SortDirection.DESCENDING, ContentSortField.RATE
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data())
                .extracting(ContentModel::getAverageRating)
                .containsExactly(4.9, 4.8, 4.5, 4.3, 3.5);
        }
    }

    @Nested
    @DisplayName("findAll() - 커서 페이지네이션")
    class PaginationTest {

        @Test
        @DisplayName("첫 페이지 조회 - hasNext=true")
        void firstPage_hasNextIsTrue() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(2);
            assertThat(response.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("인셉션", "다크나이트");
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextIdAfter()).isNotNull();
            assertThat(response.totalCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회")
        void secondPage_withCursor() {
            // given
            ContentQueryRequest firstRequest = new ContentQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );
            CursorResponse<ContentModel> firstResponse = contentQueryRepository.findAll(firstRequest);

            ContentQueryRequest secondRequest = new ContentQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> secondResponse = contentQueryRepository.findAll(secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("브레이킹 배드", "왕좌의 게임");
            assertThat(secondResponse.hasNext()).isTrue();
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext=false")
        void lastPage_hasNextIsFalse() {
            // given
            ContentQueryRequest firstRequest = new ContentQueryRequest(
                null, null, null, null, null, 2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );
            CursorResponse<ContentModel> firstResponse = contentQueryRepository.findAll(firstRequest);

            ContentQueryRequest secondRequest = new ContentQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );
            CursorResponse<ContentModel> secondResponse = contentQueryRepository.findAll(secondRequest);

            ContentQueryRequest thirdRequest = new ContentQueryRequest(
                null, null, null,
                secondResponse.nextCursor(),
                secondResponse.nextIdAfter(),
                2, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> thirdResponse = contentQueryRepository.findAll(thirdRequest);

            // then
            assertThat(thirdResponse.data()).hasSize(1);
            assertThat(thirdResponse.data().getFirst().getTitle()).isEqualTo("EPL 경기");
            assertThat(thirdResponse.hasNext()).isFalse();
            assertThat(thirdResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("내림차순 커서 페이지네이션")
        void descendingPagination() {
            // given
            ContentQueryRequest firstRequest = new ContentQueryRequest(
                null, null, null, null, null, 2, SortDirection.DESCENDING, ContentSortField.CREATED_AT
            );
            CursorResponse<ContentModel> firstResponse = contentQueryRepository.findAll(firstRequest);

            ContentQueryRequest secondRequest = new ContentQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> secondResponse = contentQueryRepository.findAll(secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("EPL 경기", "왕좌의 게임");
            assertThat(secondResponse.data())
                .extracting(ContentModel::getTitle)
                .containsExactly("브레이킹 배드", "다크나이트");
        }

        @Test
        @DisplayName("필터와 페이지네이션 조합")
        void paginationWithFilter() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                ContentType.movie, null, null, null, null, 1, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        @DisplayName("watcherCount 커서 페이지네이션")
        void paginationByWatcherCount() {
            // given
            ContentQueryRequest firstRequest = new ContentQueryRequest(
                null, null, null, null, null, 2, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );
            CursorResponse<ContentModel> firstResponse = contentQueryRepository.findAll(firstRequest);

            ContentQueryRequest secondRequest = new ContentQueryRequest(
                null, null, null,
                firstResponse.nextCursor(),
                firstResponse.nextIdAfter(),
                2, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            // when
            CursorResponse<ContentModel> secondResponse = contentQueryRepository.findAll(secondRequest);

            // then
            assertThat(firstResponse.data())
                .extracting(ContentModel::getReviewCount)
                .containsExactly(200, 180);
            assertThat(secondResponse.data())
                .extracting(ContentModel::getReviewCount)
                .containsExactly(150, 100);
        }
    }

    @Nested
    @DisplayName("findAll() - 기본값")
    class DefaultValueTest {

        @Test
        @DisplayName("limit이 null이면 기본값 20 적용")
        void withNullLimit_usesDefaultLimit() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );

            // when
            CursorResponse<ContentModel> response = contentQueryRepository.findAll(request);

            // then
            assertThat(response.data()).hasSize(5);
            assertThat(response.sortDirection()).isEqualTo(SortDirection.DESCENDING);
            assertThat(response.sortBy()).isEqualTo("POPULARITY");
        }
    }
}
