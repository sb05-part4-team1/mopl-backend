package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistContentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 이하 메서드들 cleanup batch 전용
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistContentEntity pc
            where pc.content.id in :contentIds
        """)
    int deleteAllByContentIds(@Param("contentIds") List<UUID> contentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistContentEntity pc
            where pc.playlist.id in :playlistIds
        """)
    int deleteAllByPlaylistIds(@Param("playlistIds") List<UUID> playlistIds);
}
