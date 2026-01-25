package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.playlist.JpaPlaylistRepository;
import com.mopl.jpa.repository.playlist.JpaPlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSubscriberCountSyncTxService {

    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean syncOne(UUID playlistId) {
        return jpaPlaylistRepository.findById(playlistId)
            .map(playlist -> {
                int currentCount = playlist.getSubscriberCount();
                int actualCount = jpaPlaylistSubscriberRepository.countByPlaylistId(playlistId);

                if (currentCount != actualCount) {
                    jpaPlaylistRepository.updateSubscriberCount(playlistId, actualCount);
                    log.info("[PlaylistSubscriberCountSync] synced playlistId={} from={} to={}",
                        playlistId, currentCount, actualCount);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}
