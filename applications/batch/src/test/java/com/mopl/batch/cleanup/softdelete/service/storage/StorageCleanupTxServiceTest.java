package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageCleanupTxService 단위 테스트")
class StorageCleanupTxServiceTest {

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private ContentDeletionLogRepository contentDeletionLogRepository;

    private StorageCleanupTxService txService;

    @BeforeEach
    void setUp() {
        txService = new StorageCleanupTxService(storageProvider, contentDeletionLogRepository);
    }

    @Nested
    @DisplayName("cleanupBatch()")
    class CleanupBatchTest {

        @Test
        @DisplayName("썸네일을 삭제하고 성공한 개수를 반환한다")
        void deletesThumbnailsAndReturnsCount() {
            UUID logId1 = UUID.randomUUID();
            UUID logId2 = UUID.randomUUID();
            ContentDeletionLogItem item1 = new ContentDeletionLogItem(
                logId1, UUID.randomUUID(), "/thumb1.jpg");
            ContentDeletionLogItem item2 = new ContentDeletionLogItem(
                logId2, UUID.randomUUID(), "/thumb2.jpg");
            List<ContentDeletionLogItem> targets = List.of(item1, item2);

            int result = txService.cleanupBatch(targets);

            assertThat(result).isEqualTo(2);
            verify(storageProvider).delete("/thumb1.jpg");
            verify(storageProvider).delete("/thumb2.jpg");
            verify(contentDeletionLogRepository).markImageProcessed(anyList(), any(Instant.class));
        }

        @Test
        @DisplayName("썸네일 삭제 실패 시 해당 항목을 제외하고 처리한다")
        void handlesDeleteFailureGracefully() {
            UUID logId1 = UUID.randomUUID();
            UUID logId2 = UUID.randomUUID();
            ContentDeletionLogItem item1 = new ContentDeletionLogItem(
                logId1, UUID.randomUUID(), "/fail.jpg");
            ContentDeletionLogItem item2 = new ContentDeletionLogItem(
                logId2, UUID.randomUUID(), "/success.jpg");
            List<ContentDeletionLogItem> targets = List.of(item1, item2);

            doThrow(new RuntimeException("Storage error")).when(storageProvider).delete("/fail.jpg");

            int result = txService.cleanupBatch(targets);

            assertThat(result).isEqualTo(1);
            verify(storageProvider).delete("/fail.jpg");
            verify(storageProvider).delete("/success.jpg");
        }

        @Test
        @DisplayName("모든 삭제가 실패하면 markImageProcessed를 호출하지 않는다")
        void doesNotMarkWhenAllFailed() {
            UUID logId = UUID.randomUUID();
            ContentDeletionLogItem item = new ContentDeletionLogItem(
                logId, UUID.randomUUID(), "/fail.jpg");
            List<ContentDeletionLogItem> targets = List.of(item);

            doThrow(new RuntimeException("Storage error")).when(storageProvider).delete("/fail.jpg");

            int result = txService.cleanupBatch(targets);

            assertThat(result).isZero();
            verify(contentDeletionLogRepository, never()).markImageProcessed(anyList(), any(Instant.class));
        }

        @Test
        @DisplayName("빈 리스트가 전달되면 0을 반환한다")
        void returnsZeroForEmptyList() {
            int result = txService.cleanupBatch(List.of());

            assertThat(result).isZero();
            verify(storageProvider, never()).delete(any());
            verify(contentDeletionLogRepository, never()).markImageProcessed(anyList(), any(Instant.class));
        }
    }
}
