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

    // ==================== 1. Conversation (부모: DM 포함 삭제) ====================
    public int cleanupConversations() {
        return cleanup(
            "conversation",
            props.conversation(),
            orphanCleanupRepository::findOrphanConversationIds,
            txService::cleanupConversations
        );
    }

    // ==================== 2. DirectMessage (남은 orphan) ====================
    public int cleanupDirectMessages() {
        return cleanup(
            "directMessage",
            props.directMessage(),
            orphanCleanupRepository::findOrphanDirectMessageIds,
            txService::cleanupDirectMessages
        );
    }

    // ==================== 3. Playlist (부모: Content, Subscriber 포함 삭제) ====================
    public int cleanupPlaylists() {
        return cleanup(
            "playlist",
            props.playlist(),
            orphanCleanupRepository::findOrphanPlaylistIds,
            txService::cleanupPlaylists
        );
    }

    // ==================== 4. PlaylistContent (남은 orphan) ====================
    public int cleanupPlaylistContents() {
        return cleanup(
            "playlistContent",
            props.playlistContent(),
            orphanCleanupRepository::findOrphanPlaylistContentIds,
            txService::cleanupPlaylistContents
        );
    }

    // ==================== 5. PlaylistSubscriber (남은 orphan) ====================
    public int cleanupPlaylistSubscribers() {
        return cleanup(
            "playlistSubscriber",
            props.playlistSubscriber(),
            orphanCleanupRepository::findOrphanPlaylistSubscriberIds,
            txService::cleanupPlaylistSubscribers
        );
    }

    // ==================== 6. Review ====================
    public int cleanupReviews() {
        return cleanup(
            "review",
            props.review(),
            orphanCleanupRepository::findOrphanReviewIds,
            txService::cleanupReviews
        );
    }

    // ==================== 7. ContentTag ====================
    public int cleanupContentTags() {
        return cleanup(
            "contentTag",
            props.contentTag(),
            orphanCleanupRepository::findOrphanContentTagIds,
            txService::cleanupContentTags
        );
    }

    // ==================== 8. ContentExternalMapping ====================
    public int cleanupContentExternalMappings() {
        return cleanup(
            "contentExternalMapping",
            props.contentExternalMapping(),
            orphanCleanupRepository::findOrphanContentExternalMappingIds,
            txService::cleanupContentExternalMappings
        );
    }

    // ==================== 9. Notification ====================
    public int cleanupNotifications() {
        return cleanup(
            "notification",
            props.notification(),
            orphanCleanupRepository::findOrphanNotificationIds,
            txService::cleanupNotifications
        );
    }

    // ==================== 10. Follow ====================
    public int cleanupFollows() {
        return cleanup(
            "follow",
            props.follow(),
            orphanCleanupRepository::findOrphanFollowIds,
            txService::cleanupFollows
        );
    }

    // ==================== 11. ReadStatus ====================
    public int cleanupReadStatuses() {
        return cleanup(
            "readStatus",
            props.readStatus(),
            orphanCleanupRepository::findOrphanReadStatusIds,
            txService::cleanupReadStatuses
        );
    }

    private static final int MAX_ITERATIONS = 10000;

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
        int iterations = 0;

        while (iterations < MAX_ITERATIONS) {
            List<UUID> orphanIds = findOrphans.apply(threshold, chunkSize);
            if (orphanIds.isEmpty()) {
                break;
            }

            int deleted = deleteOrphans.apply(orphanIds);
            if (deleted == 0) {
                log.warn("[OrphanCleanup] {} found {} orphans but deleted 0, breaking to prevent infinite loop",
                    name, orphanIds.size());
                break;
            }

            totalDeleted += deleted;
            iterations++;
            log.debug("[OrphanCleanup] {} deleted batch={}", name, deleted);
        }

        if (iterations >= MAX_ITERATIONS) {
            log.warn("[OrphanCleanup] {} reached max iterations={}, totalDeleted={}",
                name, MAX_ITERATIONS, totalDeleted);
        }

        return totalDeleted;
    }
}
