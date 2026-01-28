package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.denormalized.JpaDenormalizedSyncRepository;
import com.mopl.jpa.repository.playlist.JpaPlaylistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
                    log.info("[PlaylistSubscriberCountSync] synced playlistId={} from={} to={}",
                        playlistId, currentCount, actualCount);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
