package com.mopl.domain.repository.playlist;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PlaylistSubscriberRepository {

    Set<UUID> findAllPlaylistIds();

    Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds);

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    boolean save(UUID playlistId, UUID subscriberId);

    boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
