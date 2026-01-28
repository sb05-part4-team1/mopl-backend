package com.mopl.search.content.repository.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.repository.content.query.ContentSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.search.content.mapper.ContentDocumentMapper;
import com.mopl.search.document.ContentDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentQueryRepositoryEsImpl 단위 테스트")
class ContentQueryRepositoryEsImplTest {

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private SearchHits<ContentDocument> searchHits;

    private ContentQueryRepositoryEsImpl repository;

    @BeforeEach
    void setUp() {
        ContentDocumentMapper mapper = new ContentDocumentMapper();
        repository = new ContentQueryRepositoryEsImpl(operations, mapper);
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTest {

        @Test
        @DisplayName("검색 결과가 없으면 빈 응답 반환")
        void withNoResults_returnsEmptyResponse() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.sortBy()).isEqualTo("POPULARITY");
        }

        @Test
        @DisplayName("검색 결과가 limit 이하면 hasNext가 false")
        void withResultsLessThanLimit_hasNextIsFalse() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            ContentDocument doc = createContentDocument(UUID.randomUUID(), 85.0);
            SearchHit<ContentDocument> hit = createSearchHit(doc, List.of(85.0, doc.getContentId()));

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of(hit));
            given(searchHits.getTotalHits()).willReturn(1L);

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("검색 결과가 limit 초과하면 hasNext가 true")
        void withResultsMoreThanLimit_hasNextIsTrue() {
            // given
            int limit = 2;
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, limit, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            List<SearchHit<ContentDocument>> hits = new ArrayList<>();
            for (int i = 0; i < limit + 1; i++) {
                UUID id = UUID.randomUUID();
                ContentDocument doc = createContentDocument(id, 90.0 - i);
                hits.add(createSearchHit(doc, List.of(90.0 - i, id.toString())));
            }

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(hits);
            given(searchHits.getTotalHits()).willReturn(10L);

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.data()).hasSize(limit);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isNotNull();
            assertThat(result.nextIdAfter()).isNotNull();
        }

        @Test
        @DisplayName("타입 필터 적용")
        void withTypeFilter_appliesFilter() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                ContentModel.ContentType.movie, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            repository.findAll(request);

            // then
            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
            then(operations).should().search(captor.capture(), eq(ContentDocument.class));
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("키워드 검색 적용")
        void withKeyword_appliesMultiMatch() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, "인셉션", null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            repository.findAll(request);

            // then
            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
            then(operations).should().search(captor.capture(), eq(ContentDocument.class));
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("커서 기반 페이지네이션")
        void withCursor_appliesSearchAfter() {
            // given
            UUID idAfter = UUID.randomUUID();
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, "85.0", idAfter, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            repository.findAll(request);

            // then
            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
            then(operations).should().search(captor.capture(), eq(ContentDocument.class));
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("ASC 정렬 방향")
        void withAscDirection_sortsAscending() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.ASCENDING, ContentSortField.CREATED_AT
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.sortDirection()).isEqualTo(SortDirection.ASCENDING);
        }

        @Test
        @DisplayName("RATE 정렬 필드")
        void withRateSortField_sortsByAverageRating() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.RATE
            );

            ContentDocument doc = createContentDocument(UUID.randomUUID(), 85.0);
            doc = ContentDocument.builder()
                .id(doc.getId())
                .contentId(doc.getContentId())
                .type(doc.getType())
                .title(doc.getTitle())
                .averageRating(4.5)
                .createdAt(doc.getCreatedAt())
                .build();

            SearchHit<ContentDocument> hit = createSearchHit(doc, List.of(4.5, doc.getContentId()));

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of(hit));
            given(searchHits.getTotalHits()).willReturn(1L);

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.sortBy()).isEqualTo("RATE");
        }

        @Test
        @DisplayName("CREATED_AT 정렬 필드 및 커서")
        void withCreatedAtSortFieldAndCursor_parsesInstantCursor() {
            // given
            UUID idAfter = UUID.randomUUID();
            Instant cursorTime = Instant.parse("2024-01-15T10:30:00Z");
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, cursorTime.toString(), idAfter, 20, SortDirection.DESCENDING, ContentSortField.CREATED_AT
            );

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of());

            // when
            repository.findAll(request);

            // then
            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
            then(operations).should().search(captor.capture(), eq(ContentDocument.class));
        }

        @Test
        @DisplayName("sortValues가 없을 때 document에서 직접 추출")
        void withEmptySortValues_extractsFromDocument() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            UUID contentId = UUID.randomUUID();
            ContentDocument doc = createContentDocument(contentId, 85.0);
            SearchHit<ContentDocument> hit = createSearchHit(doc, List.of());

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of(hit));
            given(searchHits.getTotalHits()).willReturn(1L);

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.nextCursor()).isEqualTo("85.0");
            assertThat(result.nextIdAfter()).isEqualTo(contentId);
        }

        @Test
        @DisplayName("sortValues에서 idAfter 추출")
        void withSortValues_extractsIdAfterFromSortValues() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, 20, SortDirection.DESCENDING, ContentSortField.POPULARITY
            );

            UUID contentId = UUID.randomUUID();
            ContentDocument doc = createContentDocument(contentId, 85.0);
            SearchHit<ContentDocument> hit = createSearchHit(doc, List.of(85.0, contentId.toString()));

            given(operations.search(any(NativeQuery.class), eq(ContentDocument.class))).willReturn(searchHits);
            given(searchHits.getSearchHits()).willReturn(List.of(hit));
            given(searchHits.getTotalHits()).willReturn(1L);

            // when
            CursorResponse<ContentModel> result = repository.findAll(request);

            // then
            assertThat(result.nextCursor()).isEqualTo("85.0");
            assertThat(result.nextIdAfter()).isEqualTo(contentId);
        }
    }

    private ContentDocument createContentDocument(UUID id, double popularityScore) {
        return ContentDocument.builder()
            .id(id.toString())
            .contentId(id.toString())
            .type("movie")
            .title("테스트 콘텐츠")
            .description("테스트 설명")
            .popularityScore(popularityScore)
            .averageRating(4.0)
            .reviewCount(10)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    private SearchHit<ContentDocument> createSearchHit(ContentDocument doc, List<Object> sortValues) {
        return new SearchHit<>(
            doc.getId(),
            doc.getId(),
            null,
            1.0f,
            sortValues.toArray(),
            null,
            null,
            null,
            null,
            null,
            doc
        );
    }
}
