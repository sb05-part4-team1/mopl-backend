package com.mopl.domain.repository.playlist;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PlaylistSubscriberRepository {

    Set<UUID> findAllPlaylistIds();

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds);

    boolean save(UUID playlistId, UUID subscriberId);

    boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
