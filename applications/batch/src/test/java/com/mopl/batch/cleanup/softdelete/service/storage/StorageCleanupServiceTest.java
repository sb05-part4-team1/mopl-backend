package com.mopl.batch.cleanup.softdelete.service.storage;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties.PolicyProperties;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageCleanupService 단위 테스트")
class StorageCleanupServiceTest {

    @Mock
    private ContentDeletionLogRepository contentDeletionLogRepository;

    @Mock
    private StorageCleanupTxService storageCleanupTxService;

    @Mock
    private SoftDeleteCleanupProperties props;

    @Mock
    private SoftDeleteCleanupPolicyResolver policyResolver;

    private StorageCleanupService service;

    private static final int CHUNK_SIZE = 100;
    private static final long RETENTION_DAYS = 30L;

    @BeforeEach
    void setUp() {
        service = new StorageCleanupService(
            contentDeletionLogRepository,
            storageCleanupTxService,
            props,
            policyResolver
        );
    }

    @Nested
    @DisplayName("cleanup()")
    class CleanupTest {

        @Test
        @DisplayName("정리 대상이 없으면 0을 반환한다")
        void returnsZeroWhenNoTargets() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.storage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
            when(contentDeletionLogRepository.findImageCleanupTargets(anyInt()))
                .thenReturn(Collections.emptyList());

            int result = service.cleanup();

            assertThat(result).isZero();
            verify(storageCleanupTxService, never()).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("정리 대상을 찾아 삭제하고 삭제된 개수를 반환한다")
        void deletesTargetsAndReturnsCount() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.storage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            ContentDeletionLogItem item1 = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb1.jpg");
            ContentDeletionLogItem item2 = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb2.jpg");
            List<ContentDeletionLogItem> targets = List.of(item1, item2);

            when(contentDeletionLogRepository.findImageCleanupTargets(anyInt()))
                .thenReturn(targets)
                .thenReturn(Collections.emptyList());
            when(storageCleanupTxService.cleanupBatch(targets)).thenReturn(2);

            int result = service.cleanup();

            assertThat(result).isEqualTo(2);
            verify(storageCleanupTxService).cleanupBatch(targets);
        }

        @Test
        @DisplayName("여러 배치에 걸쳐 삭제하고 총 삭제 개수를 반환한다")
        void deletesInMultipleBatches() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.storage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            ContentDeletionLogItem item1 = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb1.jpg");
            ContentDeletionLogItem item2 = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb2.jpg");
            ContentDeletionLogItem item3 = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb3.jpg");

            List<ContentDeletionLogItem> batch1 = List.of(item1, item2);
            List<ContentDeletionLogItem> batch2 = List.of(item3);

            when(contentDeletionLogRepository.findImageCleanupTargets(anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(Collections.emptyList());
            when(storageCleanupTxService.cleanupBatch(batch1)).thenReturn(2);
            when(storageCleanupTxService.cleanupBatch(batch2)).thenReturn(1);

            int result = service.cleanup();

            assertThat(result).isEqualTo(3);
            verify(storageCleanupTxService, times(2)).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("삭제 결과가 0이면 무한루프 방지를 위해 종료한다")
        void breaksWhenDeleteReturnsZero() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.storage()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            ContentDeletionLogItem item = new ContentDeletionLogItem(
                UUID.randomUUID(), UUID.randomUUID(), "/thumb.jpg");
            List<ContentDeletionLogItem> targets = List.of(item);

            when(contentDeletionLogRepository.findImageCleanupTargets(anyInt()))
                .thenReturn(targets);
            when(storageCleanupTxService.cleanupBatch(targets)).thenReturn(0);

            int result = service.cleanup();

            assertThat(result).isZero();
            verify(storageCleanupTxService, times(1)).cleanupBatch(anyList());
        }
    }
}
