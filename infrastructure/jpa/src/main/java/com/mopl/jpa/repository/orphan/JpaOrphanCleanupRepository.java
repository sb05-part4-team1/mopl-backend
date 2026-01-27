package com.mopl.jpa.repository.orphan;

import com.mopl.jpa.entity.notification.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * FK constraint 없이 운영되는 테이블에서 orphan 레코드를 정리하기 위한 Repository.
 * 참조 대상이 hard delete 된 경우 orphan으로 판단.
 */
public interface JpaOrphanCleanupRepository extends JpaRepository<NotificationEntity, UUID> {

    // ==================== 1. Conversation (부모: DM 포함 삭제) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(c.id)
            FROM conversations c
            LEFT JOIN read_statuses rs ON c.id = rs.conversation_id
            WHERE c.created_at < :threshold
            GROUP BY c.id
            HAVING COUNT(rs.id) = 0
            ORDER BY c.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanConversationIds(Instant threshold, int limit);

    @Modifying
    @Query(value = "DELETE FROM direct_messages WHERE conversation_id IN (:conversationIds)", nativeQuery = true)
    void deleteDirectMessagesByConversationIdIn(List<UUID> conversationIds);

    @Modifying
    @Query("delete from ConversationEntity c where c.id in :ids")
    int deleteConversationsByIdIn(List<UUID> ids);

    // ==================== 2. DirectMessage (남은 orphan) ====================
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

    @Modifying
    @Query("delete from DirectMessageEntity dm where dm.id in :ids")
    int deleteDirectMessagesByIdIn(List<UUID> ids);

    // ==================== 3. Playlist (부모: Content, Subscriber 포함 삭제) ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(p.id)
            FROM playlists p
            LEFT JOIN users u ON p.owner_id = u.id
            WHERE p.created_at < :threshold
              AND u.id IS NULL
            ORDER BY p.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanPlaylistIds(Instant threshold, int limit);

    @Modifying
    @Query(value = "DELETE FROM playlist_contents WHERE playlist_id IN (:playlistIds)", nativeQuery = true)
    void deletePlaylistContentsByPlaylistIdIn(List<UUID> playlistIds);

    @Modifying
    @Query(value = "DELETE FROM playlist_subscribers WHERE playlist_id IN (:playlistIds)", nativeQuery = true)
    void deletePlaylistSubscribersByPlaylistIdIn(List<UUID> playlistIds);

    @Modifying
    @Query("delete from PlaylistEntity p where p.id in :ids")
    int deletePlaylistsByIdIn(List<UUID> ids);

    // ==================== 4. PlaylistContent (남은 orphan) ====================
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

    @Modifying
    @Query("delete from PlaylistContentEntity pc where pc.id in :ids")
    int deletePlaylistContentsByIdIn(List<UUID> ids);

    // ==================== 5. PlaylistSubscriber (남은 orphan) ====================
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

    @Query(
        value = """
            SELECT DISTINCT BIN_TO_UUID(ps.playlist_id)
            FROM playlist_subscribers ps
            INNER JOIN playlists p ON ps.playlist_id = p.id
            WHERE ps.id IN (:ids)
            """,
        nativeQuery = true
    )
    Set<UUID> findExistingPlaylistIdsBySubscriberIdIn(List<UUID> ids);

    @Modifying
    @Query("delete from PlaylistSubscriberEntity ps where ps.id in :ids")
    int deletePlaylistSubscribersByIdIn(List<UUID> ids);

    // ==================== 6. Review ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(r.id)
            FROM reviews r
            LEFT JOIN users u ON r.author_id = u.id
            LEFT JOIN contents c ON r.content_id = c.id
            WHERE r.created_at < :threshold
              AND (u.id IS NULL OR c.id IS NULL)
            ORDER BY r.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanReviewIds(Instant threshold, int limit);

    @Query(
        value = """
            SELECT DISTINCT BIN_TO_UUID(r.content_id)
            FROM reviews r
            INNER JOIN contents c ON r.content_id = c.id
            WHERE r.id IN (:ids)
            """,
        nativeQuery = true
    )
    Set<UUID> findExistingContentIdsByReviewIdIn(List<UUID> ids);

    @Modifying
    @Query("delete from ReviewEntity r where r.id in :ids")
    int deleteReviewsByIdIn(List<UUID> ids);

    // ==================== 7. ContentTag ====================
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

    @Modifying
    @Query("delete from ContentTagEntity ct where ct.id in :ids")
    int deleteContentTagsByIdIn(List<UUID> ids);

    // ==================== 8. ContentExternalMapping ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(cem.id)
            FROM content_external_mappings cem
            LEFT JOIN contents c ON cem.content_id = c.id
            WHERE cem.created_at < :threshold
              AND c.id IS NULL
            ORDER BY cem.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanContentExternalMappingIds(Instant threshold, int limit);

    @Modifying
    @Query("delete from ContentExternalMappingEntity cem where cem.id in :ids")
    int deleteContentExternalMappingsByIdIn(List<UUID> ids);

    // ==================== 9. Notification ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(n.id)
            FROM notifications n
            LEFT JOIN users u ON n.receiver_id = u.id
            WHERE n.created_at < :threshold
              AND u.id IS NULL
            ORDER BY n.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanNotificationIds(Instant threshold, int limit);

    @Modifying
    @Query("delete from NotificationEntity n where n.id in :ids")
    int deleteNotificationsByIdIn(List<UUID> ids);

    // ==================== 10. Follow ====================
    @Query(
        value = """
            SELECT BIN_TO_UUID(f.id)
            FROM follows f
            LEFT JOIN users follower ON f.follower_id = follower.id
            LEFT JOIN users followee ON f.followee_id = followee.id
            WHERE f.created_at < :threshold
              AND (follower.id IS NULL OR followee.id IS NULL)
            ORDER BY f.created_at
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findOrphanFollowIds(Instant threshold, int limit);

    @Modifying
    @Query("delete from FollowEntity f where f.id in :ids")
    int deleteFollowsByIdIn(List<UUID> ids);

    // ==================== 11. ReadStatus ====================
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

    @Modifying
    @Query("delete from ReadStatusEntity rs where rs.id in :ids")
    int deleteReadStatusesByIdIn(List<UUID> ids);
}
