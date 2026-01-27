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

    // denormalized sync batch 전용
    @Query(
        value = """
            SELECT BIN_TO_UUID(playlist_id)
            FROM (
                SELECT DISTINCT ps.playlist_id
                FROM playlist_subscribers ps
                WHERE ps.playlist_id > :lastPlaylistId
                ORDER BY ps.playlist_id
                LIMIT :limit
            ) sub
            """,
        nativeQuery = true
    )
    List<UUID> findPlaylistIdsAfter(UUID lastPlaylistId, int limit);

    @Query("""
        SELECT COUNT(ps)
        FROM PlaylistSubscriberEntity ps
        JOIN ps.subscriber
        WHERE ps.playlist.id = :playlistId
        """)
    int countByPlaylistId(UUID playlistId);
}
