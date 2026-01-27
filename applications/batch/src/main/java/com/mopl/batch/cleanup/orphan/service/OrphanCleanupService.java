package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.cleanup.orphan.config.OrphanCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphan.config.OrphanCleanupProperties;
import com.mopl.batch.cleanup.orphan.config.OrphanCleanupProperties.PolicyProperties;
import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrphanCleanupService {

    private final JpaOrphanCleanupRepository orphanCleanupRepository;
    private final OrphanCleanupTxService txService;
    private final OrphanCleanupProperties props;
    private final OrphanCleanupPolicyResolver policyResolver;

    // ==================== 1. Playlist ====================
    public int cleanupPlaylists() {
        return cleanup(
            "playlist",
            props.playlist(),
            orphanCleanupRepository::findOrphanPlaylistIds,
            txService::cleanupPlaylists
        );
    }

    // ==================== 2. Review ====================
    public int cleanupReviews() {
        return cleanup(
            "review",
            props.review(),
            orphanCleanupRepository::findOrphanReviewIds,
            txService::cleanupReviews
        );
    }

    // ==================== 3. PlaylistSubscriber ====================
    public int cleanupPlaylistSubscribers() {
        return cleanup(
            "playlistSubscriber",
            props.playlistSubscriber(),
            orphanCleanupRepository::findOrphanPlaylistSubscriberIds,
            txService::cleanupPlaylistSubscribers
        );
    }

    // ==================== 4. PlaylistContent ====================
    public int cleanupPlaylistContents() {
        return cleanup(
            "playlistContent",
            props.playlistContent(),
            orphanCleanupRepository::findOrphanPlaylistContentIds,
            txService::cleanupPlaylistContents
        );
    }

    // ==================== 5. ContentTag ====================
    public int cleanupContentTags() {
        return cleanup(
            "contentTag",
            props.contentTag(),
            orphanCleanupRepository::findOrphanContentTagIds,
            txService::cleanupContentTags
        );
    }

    // ==================== 6. ContentExternalMapping ====================
    public int cleanupContentExternalMappings() {
        return cleanup(
            "contentExternalMapping",
            props.contentExternalMapping(),
            orphanCleanupRepository::findOrphanContentExternalMappingIds,
            txService::cleanupContentExternalMappings
        );
    }

    // ==================== 7. Notification ====================
    public int cleanupNotifications() {
        return cleanup(
            "notification",
            props.notification(),
            orphanCleanupRepository::findOrphanNotificationIds,
            txService::cleanupNotifications
        );
    }

    // ==================== 8. Follow ====================
    public int cleanupFollows() {
        return cleanup(
            "follow",
            props.follow(),
            orphanCleanupRepository::findOrphanFollowIds,
            txService::cleanupFollows
        );
    }

    // ==================== 9. ReadStatus ====================
    public int cleanupReadStatuses() {
        return cleanup(
            "readStatus",
            props.readStatus(),
            orphanCleanupRepository::findOrphanReadStatusIds,
            txService::cleanupReadStatuses
        );
    }

    // ==================== 10. DirectMessage ====================
    public int cleanupDirectMessages() {
        return cleanup(
            "directMessage",
            props.directMessage(),
            orphanCleanupRepository::findOrphanDirectMessageIds,
            txService::cleanupDirectMessages
        );
    }

    // ==================== 11. Conversation ====================
    public int cleanupConversations() {
        return cleanup(
            "conversation",
            props.conversation(),
            orphanCleanupRepository::findOrphanConversationIds,
            txService::cleanupConversations
        );
    }

    private int cleanup(
        String name,
        PolicyProperties policy,
        BiFunction<Instant, Integer, List<UUID>> findOrphans,
        Function<List<UUID>, Integer> deleteOrphans
    ) {
        int chunkSize = policyResolver.chunkSize(policy);
        long gracePeriodDays = policyResolver.gracePeriodDays(policy);
        Instant threshold = Instant.now().minus(gracePeriodDays, ChronoUnit.DAYS);

        int totalDeleted = 0;

        while (true) {
            List<UUID> orphanIds = findOrphans.apply(threshold, chunkSize);
            if (orphanIds.isEmpty()) {
                break;
            }

            int deleted = deleteOrphans.apply(orphanIds);
            totalDeleted += deleted;
            log.debug("[OrphanCleanup] {} deleted batch={}", name, deleted);
        }

        return totalDeleted;
    }
}
