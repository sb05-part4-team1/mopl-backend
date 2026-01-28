package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import com.mopl.jpa.repository.playlist.JpaPlaylistRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistSubscriberCountSyncTxService {

    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaDenormalizedSyncRepository denormalizedSyncRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOne(UUID playlistId) {
        return jpaPlaylistRepository.findById(playlistId)
            .map(playlist -> {
                int currentCount = playlist.getSubscriberCount();
                int actualCount = denormalizedSyncRepository.countSubscribersByPlaylistId(playlistId);

                if (currentCount != actualCount) {
                    denormalizedSyncRepository.updateSubscriberCount(playlistId, actualCount);
                    LogContext.with("service", "playlistSubscriberCountSyncTx")
                        .and("playlistId", playlistId)
                        .and("countBefore", currentCount)
                        .and("countAfter", actualCount)
                        .debug("Count synced");
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
