package com.mopl.jpa.repository.denormalized;

import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.repository.denormalized.projection.ReviewStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * 비정규화 데이터 동기화를 위한 Repository.
 */
public interface JpaDenormalizedSyncRepository extends JpaRepository<ContentEntity, UUID> {

    // ==================== Content Review Stats ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(content_id)
            FROM (
                SELECT DISTINCT r.content_id
                FROM reviews r
                WHERE r.content_id > :lastContentId
                ORDER BY r.content_id
                LIMIT :limit
            ) sub
            """,
        nativeQuery = true
    )
    List<UUID> findContentIdsAfter(UUID lastContentId, int limit);

    @Query("""
        SELECT r.content.id AS contentId, COUNT(r) AS reviewCount, AVG(r.rating) AS averageRating
        FROM ReviewEntity r
        JOIN r.author
        WHERE r.content.id = :contentId
        GROUP BY r.content.id
        """)
    ReviewStatsProjection findReviewStatsByContentId(UUID contentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ContentEntity c
        set c.reviewCount = :reviewCount,
            c.averageRating = :averageRating,
            c.popularityScore = :popularityScore
        where c.id = :contentId
        """)
    void updateReviewStats(UUID contentId, int reviewCount, double averageRating, double popularityScore);

    // ==================== Playlist Subscriber Count ====================
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
    int countSubscribersByPlaylistId(UUID playlistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PlaylistEntity p
        set p.subscriberCount = :subscriberCount
        where p.id = :playlistId
        """)
    void updateSubscriberCount(UUID playlistId, int subscriberCount);
}
