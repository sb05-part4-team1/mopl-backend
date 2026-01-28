package com.mopl.batch.cleanup.softdelete.service.content;

import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupPolicyResolver;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties;
import com.mopl.batch.cleanup.softdelete.config.SoftDeleteCleanupProperties.PolicyProperties;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentCleanupService 단위 테스트")
class ContentCleanupServiceTest {

    @Mock
    private ContentCleanupTxService executor;

    @Mock
    private ContentCleanupRepository contentCleanupRepository;

    @Mock
    private SoftDeleteCleanupProperties props;

    @Mock
    private SoftDeleteCleanupPolicyResolver policyResolver;

    private ContentCleanupService contentCleanupService;

    private static final int CHUNK_SIZE = 100;
    private static final long RETENTION_DAYS = 30L;

    @BeforeEach
    void setUp() {
        contentCleanupService = new ContentCleanupService(
            executor,
            contentCleanupRepository,
            props,
            policyResolver
        );
    }

    private void setupPolicyResolver(PolicyProperties policy) {
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
        when(policyResolver.retentionDays(policy)).thenReturn(RETENTION_DAYS);
    }

    @Nested
    @DisplayName("cleanup()")
    class CleanupTest {

        @Test
        @DisplayName("삭제 대상이 없으면 0을 반환한다")
        void returnsZeroWhenNoTargets() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.content()).thenReturn(policy);
            setupPolicyResolver(policy);
            when(contentCleanupRepository.findCleanupTargets(any(Instant.class), anyInt()))
                .thenReturn(Collections.emptyList());

            int result = contentCleanupService.cleanup();

            assertThat(result).isZero();
            verify(executor, never()).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("삭제 대상을 찾아 삭제하고 삭제된 개수를 반환한다")
        void deletesTargetsAndReturnsCount() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.content()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> targetIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            when(contentCleanupRepository.findCleanupTargets(any(Instant.class), anyInt()))
                .thenReturn(targetIds)
                .thenReturn(Collections.emptyList());
            when(executor.cleanupBatch(targetIds)).thenReturn(2);

            int result = contentCleanupService.cleanup();

            assertThat(result).isEqualTo(2);
            verify(executor).cleanupBatch(targetIds);
        }

        @Test
        @DisplayName("여러 배치에 걸쳐 삭제하고 총 삭제 개수를 반환한다")
        void deletesInMultipleBatches() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.content()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> batch1 = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<UUID> batch2 = List.of(UUID.randomUUID());
            when(contentCleanupRepository.findCleanupTargets(any(Instant.class), anyInt()))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(Collections.emptyList());
            when(executor.cleanupBatch(batch1)).thenReturn(2);
            when(executor.cleanupBatch(batch2)).thenReturn(1);

            int result = contentCleanupService.cleanup();

            assertThat(result).isEqualTo(3);
            verify(executor, times(2)).cleanupBatch(anyList());
        }

        @Test
        @DisplayName("삭제 결과가 0이면 무한루프 방지를 위해 종료한다")
        void breaksWhenDeleteReturnsZero() {
            PolicyProperties policy = new PolicyProperties(CHUNK_SIZE, RETENTION_DAYS);
            when(props.content()).thenReturn(policy);
            setupPolicyResolver(policy);

            List<UUID> targetIds = List.of(UUID.randomUUID());
            when(contentCleanupRepository.findCleanupTargets(any(Instant.class), anyInt()))
                .thenReturn(targetIds);
            when(executor.cleanupBatch(targetIds)).thenReturn(0);

            int result = contentCleanupService.cleanup();

            assertThat(result).isZero();
            verify(executor, times(1)).cleanupBatch(anyList());
        }
    }
}
