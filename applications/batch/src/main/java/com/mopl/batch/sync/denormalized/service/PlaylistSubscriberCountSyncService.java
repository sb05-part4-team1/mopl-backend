package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.config.DenormalizedSyncPolicyResolver;
import com.mopl.batch.sync.denormalized.config.DenormalizedSyncProperties;
import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistSubscriberCountSyncService {

    private static final UUID MIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int MAX_ITERATIONS = 10000;

    private final JpaDenormalizedSyncRepository denormalizedSyncRepository;
    private final PlaylistSubscriberCountSyncTxService txService;
    private final DenormalizedSyncProperties props;
    private final DenormalizedSyncPolicyResolver policyResolver;

    public int sync() {
        int chunkSize = policyResolver.chunkSize(props.playlistSubscriberCount());
        int totalSynced = 0;
        int iterations = 0;
        UUID lastPlaylistId = MIN_UUID;

        while (iterations < MAX_ITERATIONS) {
            List<UUID> playlistIds = denormalizedSyncRepository.findPlaylistIdsAfter(lastPlaylistId, chunkSize);

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
            LogContext.with("service", "playlistSubscriberCountSync")
                .and("chunkSize", playlistIds.size())
                .and("lastPlaylistId", lastPlaylistId)
                .debug("Chunk processed");
        }

        if (iterations >= MAX_ITERATIONS) {
            LogContext.with("service", "playlistSubscriberCountSync")
                .and("maxIterations", MAX_ITERATIONS)
                .and("totalSynced", totalSynced)
                .warn("Reached max iterations");
        } else {
            LogContext.with("service", "playlistSubscriberCountSync")
                .and("iterations", iterations)
                .and("synced", totalSynced)
                .info("Sync completed");
        }

        return totalSynced;
    }
}
