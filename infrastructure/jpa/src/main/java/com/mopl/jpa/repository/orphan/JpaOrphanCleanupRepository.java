package com.mopl.jpa.repository.orphan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mopl.jpa.entity.notification.NotificationEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FK constraint 없이 운영되는 테이블에서 orphan 레코드를 정리하기 위한 Repository.
 * 참조 대상이 hard delete 된 경우 orphan으로 판단.
 */
public interface JpaOrphanCleanupRepository extends JpaRepository<NotificationEntity, UUID> {

    // ==================== Notification (receiver_id -> users) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(n.id)
            FROM notifications n
            LEFT JOIN users u ON n.receiver_id = u.id
            WHERE n.deleted_at IS NULL
              AND n.created_at < :threshold
              AND u.id IS NULL
            ORDER BY n.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanNotificationIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM notifications WHERE id IN (:ids)", nativeQuery = true)
    int deleteNotificationsByIds(List<UUID> ids);

    // ==================== Follow (follower_id, followee_id -> users) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(f.id)
            FROM follows f
            LEFT JOIN users follower ON f.follower_id = follower.id
            LEFT JOIN users followee ON f.followee_id = followee.id
            WHERE f.deleted_at IS NULL
              AND f.created_at < :threshold
              AND (follower.id IS NULL OR followee.id IS NULL)
            ORDER BY f.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanFollowIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM follows WHERE id IN (:ids)", nativeQuery = true)
    int deleteFollowsByIds(List<UUID> ids);

    // ==================== PlaylistSubscriber (playlist_id -> playlists, subscriber_id -> users) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(ps.id)
            FROM playlist_subscribers ps
            LEFT JOIN playlists p ON ps.playlist_id = p.id
            LEFT JOIN users u ON ps.subscriber_id = u.id
            WHERE ps.created_at < :threshold
              AND (p.id IS NULL OR u.id IS NULL)
            ORDER BY ps.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanPlaylistSubscriberIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM playlist_subscribers WHERE id IN (:ids)", nativeQuery = true)
    int deletePlaylistSubscribersByIds(List<UUID> ids);

    // ==================== PlaylistContent (playlist_id -> playlists, content_id -> contents) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(pc.id)
            FROM playlist_contents pc
            LEFT JOIN playlists p ON pc.playlist_id = p.id
            LEFT JOIN contents c ON pc.content_id = c.id
            WHERE pc.created_at < :threshold
              AND (p.id IS NULL OR c.id IS NULL)
            ORDER BY pc.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanPlaylistContentIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM playlist_contents WHERE id IN (:ids)", nativeQuery = true)
    int deletePlaylistContentsByIds(List<UUID> ids);

    // ==================== Playlist (owner_id -> users) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(p.id)
            FROM playlists p
            LEFT JOIN users u ON p.owner_id = u.id
            WHERE p.deleted_at IS NULL
              AND p.created_at < :threshold
              AND u.id IS NULL
            ORDER BY p.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanPlaylistIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE playlists SET deleted_at = :now WHERE id IN (:ids)", nativeQuery = true)
    int softDeletePlaylistsByIds(List<UUID> ids, Instant now);

    // ==================== Review (author_id -> users, content_id -> contents) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(r.id)
            FROM reviews r
            LEFT JOIN users u ON r.author_id = u.id
            LEFT JOIN contents c ON r.content_id = c.id
            WHERE r.deleted_at IS NULL
              AND r.created_at < :threshold
              AND (u.id IS NULL OR c.id IS NULL)
            ORDER BY r.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanReviewIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE reviews SET deleted_at = :now WHERE id IN (:ids)", nativeQuery = true)
    int softDeleteReviewsByIds(List<UUID> ids, Instant now);

    // ==================== ReadStatus (participant_id -> users, conversation_id -> conversations) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(rs.id)
            FROM read_statuses rs
            LEFT JOIN users u ON rs.participant_id = u.id
            LEFT JOIN conversations c ON rs.conversation_id = c.id
            WHERE rs.created_at < :threshold
              AND (u.id IS NULL OR c.id IS NULL)
            ORDER BY rs.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanReadStatusIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM read_statuses WHERE id IN (:ids)", nativeQuery = true)
    int deleteReadStatusesByIds(List<UUID> ids);

    // ==================== DirectMessage (conversation_id -> conversations) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(dm.id)
            FROM direct_messages dm
            LEFT JOIN conversations c ON dm.conversation_id = c.id
            WHERE dm.created_at < :threshold
              AND c.id IS NULL
            ORDER BY dm.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanDirectMessageIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM direct_messages WHERE id IN (:ids)", nativeQuery = true)
    int deleteDirectMessagesByIds(List<UUID> ids);

    // ==================== ContentTag (content_id -> contents, tag_id -> tags) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(ct.id)
            FROM content_tags ct
            LEFT JOIN contents c ON ct.content_id = c.id
            LEFT JOIN tags t ON ct.tag_id = t.id
            WHERE ct.created_at < :threshold
              AND (c.id IS NULL OR t.id IS NULL)
            ORDER BY ct.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanContentTagIds(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM content_tags WHERE id IN (:ids)", nativeQuery = true)
    int deleteContentTagsByIds(List<UUID> ids);
}
