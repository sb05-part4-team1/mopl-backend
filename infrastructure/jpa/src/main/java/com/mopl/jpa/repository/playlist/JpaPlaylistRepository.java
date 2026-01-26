package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaPlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {

    @EntityGraph(attributePaths = {"owner"})
    Optional<PlaylistEntity> findWithOwnerById(UUID id);

    // denormalized sync batch 전용
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlaylistEntity p
            set p.subscriberCount = :subscriberCount
            where p.id = :playlistId
        """)
    void updateSubscriberCount(UUID playlistId, int subscriberCount);
}
