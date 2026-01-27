package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JpaPlaylistContentRepository extends JpaRepository<PlaylistContentEntity, UUID> {

    @EntityGraph(attributePaths = {"content"})
    List<PlaylistContentEntity> findByPlaylistId(UUID playlistId);

    @EntityGraph(attributePaths = {"content"})
    List<PlaylistContentEntity> findByPlaylistIdIn(Collection<UUID> playlistIds);

    boolean existsByPlaylistIdAndContentId(UUID playlistId, UUID contentId);

    int deleteByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
}
