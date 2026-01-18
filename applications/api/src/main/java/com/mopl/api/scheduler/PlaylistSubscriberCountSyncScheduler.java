package com.mopl.api.scheduler;

import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaylistSubscriberCountSyncScheduler {

    private final PlaylistSubscriberRepository playlistSubscriberRepository;
    private final PlaylistSubscriberCountRepository playlistSubscriberCountRepository;

    @Scheduled(cron = "0 0 4 * * *")
    public void syncSubscriberCounts() {
        log.info("Starting playlist subscriber count sync...");

        Set<UUID> playlistIds = playlistSubscriberRepository.findAllPlaylistIds();
        int syncedCount = 0;

        for (UUID playlistId : playlistIds) {
            try {
                if (syncSubscriberCount(playlistId)) {
                    syncedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to sync subscriber count for playlist {}", playlistId, e);
            }
        }

        log.info(
            "Playlist subscriber count sync completed. Total: {}, Synced: {}",
            playlistIds.size(), syncedCount
        );
    }

    public boolean syncSubscriberCount(UUID playlistId) {
        long dbCount = playlistSubscriberRepository.countByPlaylistId(playlistId);
        long redisCount = playlistSubscriberCountRepository.getCount(playlistId);

        if (dbCount != redisCount) {
            playlistSubscriberCountRepository.setCount(playlistId, dbCount);
            log.debug(
                "Synced playlist {} subscriber count: Redis {} -> DB {}",
                playlistId, redisCount, dbCount
            );
            return true;
        }
        return false;
    }
}
