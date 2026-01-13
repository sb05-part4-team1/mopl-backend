package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaPlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {

    @Query("select p from PlaylistEntity p join fetch p.owner where p.id = :playlistId")
    Optional<PlaylistEntity> findByIdWithOwner(@Param("playlistId") UUID playlistId);
}
