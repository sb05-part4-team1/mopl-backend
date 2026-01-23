package com.mopl.batch.cleanup.service.playlist;

import com.mopl.batch.cleanup.properties.CleanupPolicyResolver;
import com.mopl.batch.cleanup.properties.CleanupProperties;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCleanupService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistCleanupTxService executor;
    private final CleanupProperties cleanupProperties;
    private final CleanupPolicyResolver policyResolver;

    public int cleanup() {
        int totalDeleted = 0;

        int chunkSize = policyResolver.chunkSize(cleanupProperties.getPlaylist());
        long retentionDays = policyResolver.retentionDaysRequired(cleanupProperties.getPlaylist());
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        while (true) {
            List<UUID> playlistIds = playlistRepository.findCleanupTargets(threshold, chunkSize);

            if (playlistIds.isEmpty()) {
                break;
            }

            totalDeleted += executor.cleanupBatch(playlistIds);
        }

        return totalDeleted;
    }
}
