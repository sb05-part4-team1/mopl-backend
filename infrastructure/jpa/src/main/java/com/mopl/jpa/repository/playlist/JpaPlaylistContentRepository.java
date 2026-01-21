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

    @Modifying
    @Query("""
        DELETE FROM PlaylistContentEntity pc
        WHERE pc.playlist.id = :playlistId AND pc.content.id = :contentId
        """)
    int deleteByPlaylistIdAndContentId(
        @Param("playlistId") UUID playlistId,
        @Param("contentId") UUID contentId
    );

    void deleteByPlaylist_IdAndContent_Id(UUID playlistId, UUID contentId);

    // cleanup batch 전용
    @Modifying
    @Query(
        value = """
                delete from playlist_contents
                where content_id in (:contentIds)
            """,
        nativeQuery = true
    )
    int deleteAllByContentIds(@Param("contentIds") List<UUID> contentIds);

    @Modifying
    @Query(
        value = """
                delete from playlist_contents
                where playlist_id in (:playlistIds)
            """,
        nativeQuery = true
    )
    int deleteAllByPlaylistIds(@Param("playlistIds") List<UUID> playlistIds);
}
