package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncPolicyResolver;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties;
import com.mopl.jpa.repository.playlist.JpaPlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSubscriberCountSyncService {

    private static final UUID MIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int MAX_ITERATIONS = 10000;

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final PlaylistSubscriberCountSyncTxService txService;
    private final DenormalizedSyncProperties props;
    private final DenormalizedSyncPolicyResolver policyResolver;

    public int sync() {
        int chunkSize = policyResolver.chunkSize(props.playlistSubscriberCount());
        int totalSynced = 0;
        int iterations = 0;
        UUID lastPlaylistId = MIN_UUID;

        while (iterations < MAX_ITERATIONS) {
            List<UUID> playlistIds = jpaPlaylistSubscriberRepository.findPlaylistIdsAfter(lastPlaylistId, chunkSize);

            if (playlistIds.isEmpty()) {
                break;
            }

            for (UUID playlistId : playlistIds) {
                if (txService.syncOne(playlistId)) {
                    totalSynced++;
                }
            }

            lastPlaylistId = playlistIds.getLast();
            iterations++;
            log.debug("[PlaylistSubscriberCountSync] processed chunk={} lastPlaylistId={}", playlistIds.size(), lastPlaylistId);
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[PlaylistSubscriberCountSync] reached max iterations={}, totalSynced={}", MAX_ITERATIONS, totalSynced);
        } else {
            log.info("[PlaylistSubscriberCountSync] completed iterations={} synced={}", iterations, totalSynced);
        }

        return totalSynced;
    }
}
