package com.mopl.jpa.repository.content.batch;

import com.mopl.jpa.repository.softdelete.JpaSoftDeleteCleanupRepository;
import com.mopl.jpa.repository.softdelete.projection.ContentThumbnailProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentCleanupRepositoryImpl 단위 테스트")
class ContentCleanupRepositoryImplTest {

    @Mock
    private JpaSoftDeleteCleanupRepository softDeleteCleanupRepository;

    @InjectMocks
    private ContentCleanupRepositoryImpl contentCleanupRepository;

    @Nested
    @DisplayName("findCleanupTargets()")
    class FindCleanupTargetsTest {

        @Test
        @DisplayName("threshold와 limit을 전달하여 조회한다")
        void delegatesToRepository() {
            // given
            Instant threshold = Instant.now();
            int limit = 100;
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            when(softDeleteCleanupRepository.findCleanupTargets(threshold, limit))
                .thenReturn(List.of(id1, id2));

            // when
            List<UUID> result = contentCleanupRepository.findCleanupTargets(threshold, limit);

            // then
            assertThat(result).containsExactly(id1, id2);
            verify(softDeleteCleanupRepository).findCleanupTargets(threshold, limit);
        }
    }

    @Nested
    @DisplayName("findThumbnailPathsByIdIn()")
    class FindThumbnailPathsByIdInTest {

        @Test
        @DisplayName("ID 목록으로 썸네일 경로를 조회한다")
        void returnsThumbnailPaths() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1, id2);

            ContentThumbnailProjection row1 = mock(ContentThumbnailProjection.class);
            when(row1.getId()).thenReturn(id1);
            when(row1.getThumbnailPath()).thenReturn("/thumb1.jpg");

            ContentThumbnailProjection row2 = mock(ContentThumbnailProjection.class);
            when(row2.getId()).thenReturn(id2);
            when(row2.getThumbnailPath()).thenReturn("/thumb2.jpg");

            when(softDeleteCleanupRepository.findThumbnailPathsByIds(contentIds))
                .thenReturn(List.of(row1, row2));

            // when
            Map<UUID, String> result = contentCleanupRepository.findThumbnailPathsByIdIn(contentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(id1)).isEqualTo("/thumb1.jpg");
            assertThat(result.get(id2)).isEqualTo("/thumb2.jpg");
        }

        @Test
        @DisplayName("빈 목록이면 빈 맵을 반환한다")
        void withEmptyList_returnsEmptyMap() {
            // given
            List<UUID> contentIds = List.of();
            when(softDeleteCleanupRepository.findThumbnailPathsByIds(contentIds))
                .thenReturn(List.of());

            // when
            Map<UUID, String> result = contentCleanupRepository.findThumbnailPathsByIdIn(contentIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByIdIn()")
    class DeleteByIdInTest {

        @Test
        @DisplayName("ID 목록의 콘텐츠를 삭제한다")
        void deletesContents() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1, id2);
            when(softDeleteCleanupRepository.deleteContentsByIdIn(contentIds))
                .thenReturn(2);

            // when
            int result = contentCleanupRepository.deleteByIdIn(contentIds);

            // then
            assertThat(result).isEqualTo(2);
            verify(softDeleteCleanupRepository).deleteContentsByIdIn(contentIds);
        }
    }
}
