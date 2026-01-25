package com.mopl.batch.sync.denormalized.service;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.repository.playlist.JpaPlaylistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSubscriberCountSyncTxService {

    private final JpaPlaylistRepository jpaPlaylistRepository;

    @Transactional
    public int syncBatch(List<UUID> playlistIds, Map<UUID, Long> actualCounts) {
        List<PlaylistEntity> playlists = jpaPlaylistRepository.findAllById(playlistIds);
        int synced = 0;

        for (PlaylistEntity playlist : playlists) {
            int actualCount = actualCounts.getOrDefault(playlist.getId(), 0L).intValue();
            int currentCount = playlist.getSubscriberCount();

            if (currentCount != actualCount) {
                jpaPlaylistRepository.updateSubscriberCount(playlist.getId(), actualCount);
                log.info("[PlaylistSubscriberCountSync] synced playlistId={} from={} to={}",
                    playlist.getId(), currentCount, actualCount);
                synced++;
            }
        }

        return synced;
    }
}
