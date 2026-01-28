package com.mopl.batch.cleanup.softdelete.service.content;

import com.mopl.batch.cleanup.softdelete.strategy.content.ContentDeletionStrategy;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentCleanupTxService 단위 테스트")
class ContentCleanupTxServiceTest {

    @Mock
    private ContentCleanupRepository contentCleanupRepository;

    @Mock
    private ContentDeletionStrategy deletionStrategy;

    @Mock
    private AfterCommitExecutor afterCommitExecutor;

    @Mock
    private ContentSearchSyncPort contentSearchSyncPort;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private ContentCleanupTxService txService;

    @BeforeEach
    void setUp() {
        txService = new ContentCleanupTxService(
            contentCleanupRepository,
            deletionStrategy,
            afterCommitExecutor,
            contentSearchSyncPort
        );
    }

    @Nested
    @DisplayName("cleanupBatch()")
    class CleanupBatchTest {

        @Test
        @DisplayName("콘텐츠를 삭제하고 삭제된 개수를 반환한다")
        void deletesContentsAndReturnsCount() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1, id2);

            Map<UUID, String> thumbnailPaths = Map.of(
                id1, "/thumbnails/1.jpg",
                id2, "/thumbnails/2.jpg"
            );

            when(contentCleanupRepository.findThumbnailPathsByIdIn(contentIds)).thenReturn(thumbnailPaths);
            when(deletionStrategy.onDeleted(thumbnailPaths)).thenReturn(2);
            when(contentCleanupRepository.deleteByIdIn(contentIds)).thenReturn(2);

            int result = txService.cleanupBatch(contentIds);

            assertThat(result).isEqualTo(2);
            verify(contentCleanupRepository).deleteByIdIn(contentIds);
            verify(deletionStrategy).onDeleted(thumbnailPaths);
        }

        @Test
        @DisplayName("삭제 후 Elasticsearch 동기화를 afterCommit으로 예약한다")
        void schedulesElasticsearchSyncAfterCommit() {
            UUID id1 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1);

            when(contentCleanupRepository.findThumbnailPathsByIdIn(contentIds)).thenReturn(Map.of());
            when(deletionStrategy.onDeleted(anyMap())).thenReturn(0);
            when(contentCleanupRepository.deleteByIdIn(contentIds)).thenReturn(1);

            txService.cleanupBatch(contentIds);

            verify(afterCommitExecutor).execute(runnableCaptor.capture());
            Runnable captured = runnableCaptor.getValue();

            captured.run();

            verify(contentSearchSyncPort).deleteAll(contentIds);
        }

        @Test
        @DisplayName("썸네일이 없는 콘텐츠도 정상 삭제된다")
        void deletesContentsWithoutThumbnails() {
            UUID id1 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1);

            when(contentCleanupRepository.findThumbnailPathsByIdIn(contentIds)).thenReturn(Map.of());
            when(deletionStrategy.onDeleted(anyMap())).thenReturn(0);
            when(contentCleanupRepository.deleteByIdIn(contentIds)).thenReturn(1);

            int result = txService.cleanupBatch(contentIds);

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제 수와 요청 수가 다르면 로그를 남긴다 (결과는 실제 삭제 수)")
        void logsWhenDeleteCountMismatch() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(id1, id2);

            when(contentCleanupRepository.findThumbnailPathsByIdIn(contentIds)).thenReturn(Map.of());
            when(deletionStrategy.onDeleted(anyMap())).thenReturn(0);
            when(contentCleanupRepository.deleteByIdIn(contentIds)).thenReturn(1);

            int result = txService.cleanupBatch(contentIds);

            assertThat(result).isEqualTo(1);
        }
    }
}
