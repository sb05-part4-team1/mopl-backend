package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.UUID;

public interface JpaPlaylistSubscriberRepository extends
    JpaRepository<PlaylistSubscriberEntity, UUID> {

    @Query("SELECT DISTINCT ps.playlist.id FROM PlaylistSubscriberEntity ps")
    Set<UUID> findAllPlaylistIds();

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
