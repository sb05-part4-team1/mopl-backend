package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncTxService;
import com.mopl.batch.sync.denormalized.service.PlaylistSubscriberCountSyncTxService;
import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrphanCleanupTxService {

    private final JpaOrphanCleanupRepository orphanCleanupRepository;
    private final ContentReviewStatsSyncTxService contentReviewStatsSyncTxService;
    private final PlaylistSubscriberCountSyncTxService playlistSubscriberCountSyncTxService;

    // ==================== 1. Conversation (부모: DM 포함 삭제) ====================
    @Transactional
    public int cleanupConversations(List<UUID> conversationIds) {
        orphanCleanupRepository.deleteDirectMessagesByConversationIdIn(conversationIds);
        return orphanCleanupRepository.deleteConversationsByIdIn(conversationIds);
    }

    // ==================== 2. DirectMessage (남은 orphan) ====================
    @Transactional
    public int cleanupDirectMessages(List<UUID> directMessageIds) {
        return orphanCleanupRepository.deleteDirectMessagesByIdIn(directMessageIds);
    }

    // ==================== 3. Playlist (부모: Content, Subscriber 포함 삭제) ====================
    @Transactional
    public int cleanupPlaylists(List<UUID> playlistIds) {
        orphanCleanupRepository.deletePlaylistContentsByPlaylistIdIn(playlistIds);
        orphanCleanupRepository.deletePlaylistSubscribersByPlaylistIdIn(playlistIds);
        return orphanCleanupRepository.deletePlaylistsByIdIn(playlistIds);
    }

    // ==================== 4. PlaylistContent (남은 orphan) ====================
    @Transactional
    public int cleanupPlaylistContents(List<UUID> playlistContentIds) {
        return orphanCleanupRepository.deletePlaylistContentsByIdIn(playlistContentIds);
    }

    // ==================== 5. PlaylistSubscriber (남은 orphan) ====================
    @Transactional
    public int cleanupPlaylistSubscribers(List<UUID> playlistSubscriberIds) {
        Set<UUID> playlistIds = orphanCleanupRepository.findExistingPlaylistIdsBySubscriberIdIn(playlistSubscriberIds);
        int deleted = orphanCleanupRepository.deletePlaylistSubscribersByIdIn(playlistSubscriberIds);
        playlistIds.forEach(playlistSubscriberCountSyncTxService::syncOne);
        return deleted;
    }

    // ==================== 6. Review ====================
    @Transactional
    public int cleanupReviews(List<UUID> reviewIds) {
        Set<UUID> contentIds = orphanCleanupRepository.findExistingContentIdsByReviewIdIn(reviewIds);
        int deleted = orphanCleanupRepository.deleteReviewsByIdIn(reviewIds);
        contentIds.forEach(contentReviewStatsSyncTxService::syncOne);
        return deleted;
    }

    // ==================== 7. ContentTag ====================
    @Transactional
    public int cleanupContentTags(List<UUID> contentTagIds) {
        return orphanCleanupRepository.deleteContentTagsByIdIn(contentTagIds);
    }

    // ==================== 8. ContentExternalMapping ====================
    @Transactional
    public int cleanupContentExternalMappings(List<UUID> contentExternalMappingIds) {
        return orphanCleanupRepository.deleteContentExternalMappingsByIdIn(contentExternalMappingIds);
    }

    // ==================== 9. Notification ====================
    @Transactional
    public int cleanupNotifications(List<UUID> notificationIds) {
        return orphanCleanupRepository.deleteNotificationsByIdIn(notificationIds);
    }

    // ==================== 10. Follow ====================
    @Transactional
    public int cleanupFollows(List<UUID> followIds) {
        return orphanCleanupRepository.deleteFollowsByIdIn(followIds);
    }

    // ==================== 11. ReadStatus ====================
    @Transactional
    public int cleanupReadStatuses(List<UUID> readStatusIds) {
        return orphanCleanupRepository.deleteReadStatusesByIdIn(readStatusIds);
    }
}
