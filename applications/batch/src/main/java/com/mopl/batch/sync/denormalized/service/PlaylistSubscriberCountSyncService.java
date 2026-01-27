package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.repository.playlist.JpaPlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSubscriberCountSyncService {

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final PlaylistSubscriberCountSyncTxService txService;

    public int sync() {
        Set<UUID> playlistIds = jpaPlaylistSubscriberRepository.findAllPlaylistIds();

        if (playlistIds.isEmpty()) {
            log.info("[PlaylistSubscriberCountSync] no playlists with subscribers found");
            return 0;
        }

        int totalSynced = 0;
        for (UUID playlistId : playlistIds) {
            if (txService.syncOne(playlistId)) {
                totalSynced++;
            }
        }

        log.info("[PlaylistSubscriberCountSync] completed total={} synced={}", playlistIds.size(), totalSynced);
        return totalSynced;
    }
}
