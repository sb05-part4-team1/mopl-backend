package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncPolicyResolver;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties.PolicyProperties;
import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentReviewStatsSyncServiceTest {

    @Mock
    private JpaDenormalizedSyncRepository denormalizedSyncRepository;

    @Mock
    private ContentReviewStatsSyncTxService txService;

    @Mock
    private DenormalizedSyncProperties props;

    @Mock
    private DenormalizedSyncPolicyResolver policyResolver;

    private ContentReviewStatsSyncService syncService;

    private static final int CHUNK_SIZE = 100;

    @BeforeEach
    void setUp() {
        syncService = new ContentReviewStatsSyncService(
            denormalizedSyncRepository,
            txService,
            props,
            policyResolver
        );
    }

    @Test
    @DisplayName("동기화할 Content가 없으면 0을 반환한다")
    void returnsZeroWhenNoContent() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.contentReviewStats()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
        when(denormalizedSyncRepository.findContentIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(Collections.emptyList());

        int result = syncService.sync();

        assertThat(result).isZero();
        verify(txService, never()).syncOne(any());
    }

    @Test
    @DisplayName("Content 리뷰 통계를 동기화하고 동기화된 개수를 반환한다")
    void syncsContentReviewStats() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.contentReviewStats()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID contentId1 = UUID.randomUUID();
        UUID contentId2 = UUID.randomUUID();
        List<UUID> contentIds = List.of(contentId1, contentId2);

        when(denormalizedSyncRepository.findContentIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(contentIds)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(contentId1)).thenReturn(true);
        when(txService.syncOne(contentId2)).thenReturn(true);

        int result = syncService.sync();

        assertThat(result).isEqualTo(2);
        verify(txService).syncOne(contentId1);
        verify(txService).syncOne(contentId2);
    }

    @Test
    @DisplayName("동기화되지 않은 Content는 카운트하지 않는다")
    void doesNotCountUnsyncedContent() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.contentReviewStats()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID contentId1 = UUID.randomUUID();
        UUID contentId2 = UUID.randomUUID();
        List<UUID> contentIds = List.of(contentId1, contentId2);

        when(denormalizedSyncRepository.findContentIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(contentIds)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(contentId1)).thenReturn(true);
        when(txService.syncOne(contentId2)).thenReturn(false);

        int result = syncService.sync();

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 배치에 걸쳐 동기화한다")
    void syncsInMultipleBatches() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.contentReviewStats()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID contentId1 = UUID.randomUUID();
        UUID contentId2 = UUID.randomUUID();
        List<UUID> batch1 = List.of(contentId1);
        List<UUID> batch2 = List.of(contentId2);

        when(denormalizedSyncRepository.findContentIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(batch1)
            .thenReturn(batch2)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(any())).thenReturn(true);

        int result = syncService.sync();

        assertThat(result).isEqualTo(2);
        verify(txService, times(2)).syncOne(any());
    }
}
