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
class PlaylistSubscriberCountSyncServiceTest {

    @Mock
    private JpaDenormalizedSyncRepository denormalizedSyncRepository;

    @Mock
    private PlaylistSubscriberCountSyncTxService txService;

    @Mock
    private DenormalizedSyncProperties props;

    @Mock
    private DenormalizedSyncPolicyResolver policyResolver;

    private PlaylistSubscriberCountSyncService syncService;

    private static final int CHUNK_SIZE = 100;

    @BeforeEach
    void setUp() {
        syncService = new PlaylistSubscriberCountSyncService(
            denormalizedSyncRepository,
            txService,
            props,
            policyResolver
        );
    }

    @Test
    @DisplayName("동기화할 Playlist가 없으면 0을 반환한다")
    void returnsZeroWhenNoPlaylist() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.playlistSubscriberCount()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);
        when(denormalizedSyncRepository.findPlaylistIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(Collections.emptyList());

        int result = syncService.sync();

        assertThat(result).isZero();
        verify(txService, never()).syncOne(any());
    }

    @Test
    @DisplayName("Playlist 구독자 수를 동기화하고 동기화된 개수를 반환한다")
    void syncsPlaylistSubscriberCount() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.playlistSubscriberCount()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID playlistId1 = UUID.randomUUID();
        UUID playlistId2 = UUID.randomUUID();
        List<UUID> playlistIds = List.of(playlistId1, playlistId2);

        when(denormalizedSyncRepository.findPlaylistIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(playlistIds)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(playlistId1)).thenReturn(true);
        when(txService.syncOne(playlistId2)).thenReturn(true);

        int result = syncService.sync();

        assertThat(result).isEqualTo(2);
        verify(txService).syncOne(playlistId1);
        verify(txService).syncOne(playlistId2);
    }

    @Test
    @DisplayName("동기화되지 않은 Playlist는 카운트하지 않는다")
    void doesNotCountUnsyncedPlaylist() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.playlistSubscriberCount()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID playlistId1 = UUID.randomUUID();
        UUID playlistId2 = UUID.randomUUID();
        List<UUID> playlistIds = List.of(playlistId1, playlistId2);

        when(denormalizedSyncRepository.findPlaylistIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(playlistIds)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(playlistId1)).thenReturn(true);
        when(txService.syncOne(playlistId2)).thenReturn(false);

        int result = syncService.sync();

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 배치에 걸쳐 동기화한다")
    void syncsInMultipleBatches() {
        PolicyProperties policy = new PolicyProperties(CHUNK_SIZE);
        when(props.playlistSubscriberCount()).thenReturn(policy);
        when(policyResolver.chunkSize(policy)).thenReturn(CHUNK_SIZE);

        UUID playlistId1 = UUID.randomUUID();
        UUID playlistId2 = UUID.randomUUID();
        List<UUID> batch1 = List.of(playlistId1);
        List<UUID> batch2 = List.of(playlistId2);

        when(denormalizedSyncRepository.findPlaylistIdsAfter(any(UUID.class), anyInt()))
            .thenReturn(batch1)
            .thenReturn(batch2)
            .thenReturn(Collections.emptyList());
        when(txService.syncOne(any())).thenReturn(true);

        int result = syncService.sync();

        assertThat(result).isEqualTo(2);
        verify(txService, times(2)).syncOne(any());
    }
}
