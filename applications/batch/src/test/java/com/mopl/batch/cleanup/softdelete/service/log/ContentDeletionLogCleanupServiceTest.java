package com.mopl.batch.cleanup.softdelete.service.log;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties.PolicyProperties;
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
@DisplayName("ContentDeletionLogCleanupService 단위 테스트")
class ContentDeletionLogCleanupServiceTest {

    @Mock
    private ContentDeletionLogRepository contentDeletionLogRepository;

    @Mock
    private ContentDeletionLogCleanupTxService executor;

    @Mock
    private SoftDeleteCleanupProperties props;

    @Mock
    private SoftDeleteCleanupPolicyResolver policyResolver;

    private ContentDeletionLogCleanupService service;

    private static final int CHUNK_SIZE = 100;
    private static final long RETENTION_DAYS = 30L;

    @BeforeEach
    void setUp() {
        service = new ContentDeletionLogCleanupService(
            contentDeletionLogRepository,
            executor,
            props,
            policyResolver
        );
    }

    @Nested
    @DisplayName("cleanup()")
    class CleanupTest {

        @Test
        @DisplayName("처리 완료된 로그가 없으면 0을 반환한다")
        void returnsZeroWhenNoProcessedLogs() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.deletionLog()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
            when(contentDeletionLogRepository.findFullyProcessedLogIds(anyInt()))
                .thenReturn(Collections.emptyList());

            int result = service.cleanup();

            assertThat(result).isZero();
            verify(executor, never()).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("처리 완료된 로그를 삭제하고 삭제된 개수를 반환한다")
        void deletesProcessedLogsAndReturnsCount() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.deletionLog()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<UUID> logIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            when(contentDeletionLogRepository.findFullyProcessedLogIds(anyInt()))
                .thenReturn(logIds)
                .thenReturn(Collections.emptyList());
            when(executor.cleanupBatch(logIds)).thenReturn(2);

            int result = service.cleanup();

            assertThat(result).isEqualTo(2);
            verify(executor).cleanupBatch(logIds);
        }

        @Test
        @DisplayName("여러 배치에 걸쳐 삭제하고 총 삭제 개수를 반환한다")
        void deletesInMultipleBatches() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.deletionLog()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<UUID> batch1 = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<UUID> batch2 = List.of(UUID.randomUUID());
            when(contentDeletionLogRepository.findFullyProcessedLogIds(anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(Collections.emptyList());
            when(executor.cleanupBatch(batch1)).thenReturn(2);
            when(executor.cleanupBatch(batch2)).thenReturn(1);

            int result = service.cleanup();

            assertThat(result).isEqualTo(3);
            verify(executor, times(2)).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("삭제 결과가 0이면 무한루프 방지를 위해 종료한다")
        void breaksWhenDeleteReturnsZero() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.deletionLog()).thenReturn(policy);
            when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

            List<UUID> logIds = List.of(UUID.randomUUID());
            when(contentDeletionLogRepository.findFullyProcessedLogIds(anyInt()))
                .thenReturn(logIds);
            when(executor.cleanupBatch(logIds)).thenReturn(0);

            int result = service.cleanup();

            assertThat(result).isZero();
            verify(executor, times(1)).cleanupBatch(anyList());
        }
    }
}
