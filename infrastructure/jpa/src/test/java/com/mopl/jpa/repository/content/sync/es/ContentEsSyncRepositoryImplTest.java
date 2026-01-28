package com.mopl.jpa.repository.content.sync.es;

import com.mopl.domain.model.content.ContentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentEsSyncRepositoryImpl 단위 테스트")
class ContentEsSyncRepositoryImplTest {

    @Mock
    private JpaContentEsSyncRepository jpaRepository;

    @InjectMocks
    private ContentEsSyncRepositoryImpl contentEsSyncRepository;

    @Nested
    @DisplayName("findSyncTargets()")
    class FindSyncTargetsTest {

        @Test
        @DisplayName("파라미터를 전달하여 조회하고 결과를 변환한다")
        void delegatesAndConverts() {
            // given
            Instant lastCreatedAt = Instant.now();
            String lastId = UUID.randomUUID().toString();
            int limit = 100;

            UUID contentId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            ContentEsSyncRow row = mock(ContentEsSyncRow.class);
            when(row.getId()).thenReturn(contentId);
            when(row.getType()).thenReturn("movie");
            when(row.getTitle()).thenReturn("Test Movie");
            when(row.getDescription()).thenReturn("Test Description");
            when(row.getThumbnailPath()).thenReturn("/thumb.jpg");
            when(row.getReviewCount()).thenReturn(10);
            when(row.getAverageRating()).thenReturn(4.5);
            when(row.getPopularityScore()).thenReturn(100.0);
            when(row.getCreatedAt()).thenReturn(createdAt);
            when(row.getUpdatedAt()).thenReturn(updatedAt);

            when(jpaRepository.findSyncTargets(lastCreatedAt, lastId, limit))
                .thenReturn(List.of(row));

            // when
            List<ContentModel> result = contentEsSyncRepository.findSyncTargets(lastCreatedAt, lastId, limit);

            // then
            assertThat(result).hasSize(1);
            ContentModel model = result.getFirst();
            assertThat(model.getId()).isEqualTo(contentId);
            assertThat(model.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(model.getTitle()).isEqualTo("Test Movie");
            assertThat(model.getDescription()).isEqualTo("Test Description");
            assertThat(model.getThumbnailPath()).isEqualTo("/thumb.jpg");
            assertThat(model.getReviewCount()).isEqualTo(10);
            assertThat(model.getAverageRating()).isEqualTo(4.5);
            assertThat(model.getPopularityScore()).isEqualTo(100.0);
            assertThat(model.getCreatedAt()).isEqualTo(createdAt);
            assertThat(model.getUpdatedAt()).isEqualTo(updatedAt);
            verify(jpaRepository).findSyncTargets(lastCreatedAt, lastId, limit);
        }

        @Test
        @DisplayName("null 파라미터로도 조회할 수 있다")
        void withNullParams_works() {
            // given
            when(jpaRepository.findSyncTargets(null, null, 10))
                .thenReturn(List.of());

            // when
            List<ContentModel> result = contentEsSyncRepository.findSyncTargets(null, null, 10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 결과를 모두 변환한다")
        void withMultipleResults_convertsAll() {
            // given
            ContentEsSyncRow row1 = createMockRow(UUID.randomUUID(), "movie");
            ContentEsSyncRow row2 = createMockRow(UUID.randomUUID(), "tvSeries");

            when(jpaRepository.findSyncTargets(null, null, 10))
                .thenReturn(List.of(row1, row2));

            // when
            List<ContentModel> result = contentEsSyncRepository.findSyncTargets(null, null, 10);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(result.get(1).getType()).isEqualTo(ContentModel.ContentType.tvSeries);
        }

        private ContentEsSyncRow createMockRow(UUID id, String type) {
            ContentEsSyncRow row = mock(ContentEsSyncRow.class);
            when(row.getId()).thenReturn(id);
            when(row.getType()).thenReturn(type);
            when(row.getTitle()).thenReturn("Title");
            when(row.getDescription()).thenReturn("Description");
            when(row.getThumbnailPath()).thenReturn("/thumb.jpg");
            when(row.getReviewCount()).thenReturn(0);
            when(row.getAverageRating()).thenReturn(0.0);
            when(row.getPopularityScore()).thenReturn(0.0);
            when(row.getCreatedAt()).thenReturn(Instant.now());
            when(row.getUpdatedAt()).thenReturn(Instant.now());
            return row;
        }
    }
}
