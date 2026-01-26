package com.mopl.domain.repository.playlist;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlaylistSubscriberRepository {

    Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds);

    List<UUID> findSubscriberIdsByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    void save(UUID playlistId, UUID subscriberId);

    boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    // cleanup batch 전용
    int deleteAllByPlaylistIds(List<UUID> playlistIds);
}
