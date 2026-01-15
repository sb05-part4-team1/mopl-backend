package com.mopl.domain.repository.playlist;

import java.util.Set;
import java.util.UUID;

public interface PlaylistSubscriberRepository {

    Set<UUID> findAllPlaylistIds();

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    void save(UUID playlistId, UUID subscriberId);

    void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
