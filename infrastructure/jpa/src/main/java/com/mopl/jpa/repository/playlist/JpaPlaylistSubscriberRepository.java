package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface JpaPlaylistSubscriberRepository extends
    JpaRepository<PlaylistSubscriberEntity, UUID> {

    @Query("SELECT DISTINCT ps.playlist.id FROM PlaylistSubscriberEntity ps")
    Set<UUID> findAllPlaylistIds();

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId);

    @Modifying
    @Query("""
        DELETE FROM PlaylistSubscriberEntity ps
        WHERE ps.playlist.id = :playlistId AND ps.subscriber.id = :subscriberId
        """)
    int deleteByPlaylistIdAndSubscriberId(
        @Param("playlistId") UUID playlistId,
        @Param("subscriberId") UUID subscriberId
    );

    @Query("""
        SELECT ps.playlist.id
        FROM PlaylistSubscriberEntity ps
        WHERE ps.subscriber.id = :subscriberId AND ps.playlist.id IN :playlistIds
        """)
    Set<UUID> findPlaylistIdsBySubscriberIdAndPlaylistIdIn(
        @Param("subscriberId") UUID subscriberId,
        @Param("playlistIds") Collection<UUID> playlistIds
    );

    // 이하 메서드들 cleanup batch 전용
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistSubscriberEntity ps
            where ps.playlist.id in :playlistIds
        """)
    int deleteAllByPlaylistIds(@Param("playlistIds") List<UUID> playlistIds);
}
