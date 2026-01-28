package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JpaPlaylistSubscriberRepository extends
    JpaRepository<PlaylistSubscriberEntity, UUID> {

    @Query("""
        SELECT ps.playlist.id
        FROM PlaylistSubscriberEntity ps
        WHERE ps.subscriber.id = :subscriberId AND ps.playlist.id IN :playlistIds
        """)
    Set<UUID> findPlaylistIdsBySubscriberIdAndPlaylistIdIn(
        UUID subscriberId,
        Collection<UUID> playlistIds
    );

    @Query("""
        SELECT ps.subscriber.id
        FROM PlaylistSubscriberEntity ps
        JOIN ps.subscriber
        WHERE ps.playlist.id = :playlistId
        """)
    List<UUID> findSubscriberIdsByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    int deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);
}
