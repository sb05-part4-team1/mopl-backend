package com.mopl.domain.repository.playlist;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.List;
import java.util.UUID;

public interface PlaylistSubscriberRepository {

    Set<UUID> findAllPlaylistIds();

    Set<UUID> findSubscribedPlaylistIds(UUID subscriberId, Collection<UUID> playlistIds);

    List<UUID> findSubscriberIdsByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    void save(UUID playlistId, UUID subscriberId);

    boolean deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    // 이하 메서드들 cleanup batch 전용
    int deleteAllByPlaylistIds(List<UUID> playlistIds);
}
