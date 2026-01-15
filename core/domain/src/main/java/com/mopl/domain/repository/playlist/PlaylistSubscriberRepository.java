package com.mopl.domain.repository.playlist;

import java.util.UUID;

public interface PlaylistSubscriberRepository {

    void save(UUID playlistId, UUID subscriberId);

    void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
