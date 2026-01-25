package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaPlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {

    @EntityGraph(attributePaths = {"owner"})
    Optional<PlaylistEntity> findWithOwnerById(UUID id);

    @Query(
        value = """
                select BIN_TO_UUID(id)
                from playlists
                where deleted_at is not null
                  and deleted_at < :threshold
                order by deleted_at
                limit :limit
            """,
        nativeQuery = true
    )
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
                delete from playlists
                where id in (:playlistIds)
            """,
        nativeQuery = true
    )
    int deleteByIdIn(List<UUID> playlistIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlaylistEntity p
            set p.subscriberCount = :subscriberCount
            where p.id = :playlistId
        """)
    int updateSubscriberCount(UUID playlistId, int subscriberCount);
}
