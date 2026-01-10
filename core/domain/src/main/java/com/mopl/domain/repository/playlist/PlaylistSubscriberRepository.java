package com.mopl.domain.repository.playlist;

import java.util.UUID;

public interface PlaylistSubscriberRepository {

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    void save(UUID playlistId, UUID subscriberId);

    void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

}
