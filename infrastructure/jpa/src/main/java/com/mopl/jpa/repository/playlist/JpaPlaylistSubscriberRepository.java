package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JpaPlaylistSubscriberRepository extends
    JpaRepository<PlaylistSubscriberEntity, UUID> {

    @Query("SELECT DISTINCT ps.playlist.id FROM PlaylistSubscriberEntity ps")
    Set<UUID> findAllPlaylistIds();

    @Query("""
        SELECT ps.playlist.id
        FROM PlaylistSubscriberEntity ps
        WHERE ps.subscriber.id = :subscriberId AND ps.playlist.id IN :playlistIds
        """)
    Set<UUID> findPlaylistIdsBySubscriberIdAndPlaylistIdIn(
        UUID subscriberId,
        Collection<UUID> playlistIds
    );

    @Query("SELECT ps.subscriber.id FROM PlaylistSubscriberEntity ps WHERE ps.playlist.id = :playlistId")
    List<UUID> findSubscriberIdsByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);

    @Query("""
        SELECT ps.playlist.id, COUNT(ps)
        FROM PlaylistSubscriberEntity ps
        WHERE ps.playlist.id IN :playlistIds
        GROUP BY ps.playlist.id
        """)
    List<Object[]> countByPlaylistIdIn(Collection<UUID> playlistIds);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM PlaylistSubscriberEntity ps
        WHERE ps.playlist.id = :playlistId AND ps.subscriber.id = :subscriberId
        """)
    int deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistSubscriberEntity ps
            where ps.playlist.id in :playlistIds
        """)
    int deleteAllByPlaylistIds(List<UUID> playlistIds);
}
