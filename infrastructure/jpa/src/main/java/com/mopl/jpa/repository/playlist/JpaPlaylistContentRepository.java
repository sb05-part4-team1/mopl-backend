package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPlaylistContentRepository extends JpaRepository<PlaylistContentEntity, UUID> {

    boolean existsByPlaylist_IdAndContent_Id(UUID playlistId, UUID contentId);

    void deleteByPlaylist_IdAndContent_Id(UUID playlistId, UUID contentId);
}
