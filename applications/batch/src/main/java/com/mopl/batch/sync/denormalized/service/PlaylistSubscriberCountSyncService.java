package com.mopl.batch.sync.denormalized.service;

import com.mopl.batch.sync.denormalized.properties.DenormalizedSyncPolicyResolver;
import com.mopl.jpa.repository.playlist.JpaPlaylistSubscriberRepository;
import com.mopl.jpa.repository.playlist.projection.PlaylistSubscriberCountProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSubscriberCountSyncService {

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final PlaylistSubscriberCountSyncTxService txService;
    private final DenormalizedSyncPolicyResolver policyResolver;

    public int sync() {
        Set<UUID> playlistIds = jpaPlaylistSubscriberRepository.findAllPlaylistIds();

        if (playlistIds.isEmpty()) {
            log.info("[PlaylistSubscriberCountSync] no playlists with subscribers found");
            return 0;
        }

        int batchSize = policyResolver.resolve().batchSize();
        List<UUID> playlistIdList = new ArrayList<>(playlistIds);
        int totalSynced = 0;

        for (int i = 0; i < playlistIdList.size(); i += batchSize) {
            List<UUID> batch = playlistIdList.subList(i, Math.min(i + batchSize, playlistIdList.size()));

            Map<UUID, Long> actualCounts = jpaPlaylistSubscriberRepository.countByPlaylistIdIn(batch)
                .stream()
                .collect(Collectors.toMap(
                    PlaylistSubscriberCountProjection::getPlaylistId,
                    PlaylistSubscriberCountProjection::getSubscriberCount
                ));

            int synced = txService.syncBatch(batch, actualCounts);
            totalSynced += synced;

            log.debug("[PlaylistSubscriberCountSync] batch processed offset={} batchSize={} synced={}",
                i, batch.size(), synced);
        }

        return totalSynced;
    }
}
