package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.cleanup.orphan.properties.OrphanCleanupPolicyProperties;
import com.mopl.batch.cleanup.orphan.properties.OrphanCleanupPolicyResolver;
import com.mopl.batch.cleanup.orphan.properties.OrphanCleanupProperties;
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
    private final OrphanCleanupProperties cleanupProperties;
    private final OrphanCleanupPolicyResolver policyResolver;

    // ==================== 1. Playlist ====================
    public int cleanupPlaylists() {
        return cleanup(
            "playlist",
            cleanupProperties.getPlaylist(),
            orphanCleanupRepository::findOrphanPlaylistIds,
            txService::cleanupPlaylists
        );
    }

    // ==================== 2. Review ====================
    public int cleanupReviews() {
        return cleanup(
            "review",
            cleanupProperties.getReview(),
            orphanCleanupRepository::findOrphanReviewIds,
            txService::cleanupReviews
        );
    }

    // ==================== 3. PlaylistSubscriber ====================
    public int cleanupPlaylistSubscribers() {
        return cleanup(
            "playlistSubscriber",
            cleanupProperties.getPlaylistSubscriber(),
            orphanCleanupRepository::findOrphanPlaylistSubscriberIds,
            txService::cleanupPlaylistSubscribers
        );
    }

    // ==================== 4. PlaylistContent ====================
    public int cleanupPlaylistContents() {
        return cleanup(
            "playlistContent",
            cleanupProperties.getPlaylistContent(),
            orphanCleanupRepository::findOrphanPlaylistContentIds,
            txService::cleanupPlaylistContents
        );
    }

    // ==================== 5. ContentTag ====================
    public int cleanupContentTags() {
        return cleanup(
            "contentTag",
            cleanupProperties.getContentTag(),
            orphanCleanupRepository::findOrphanContentTagIds,
            txService::cleanupContentTags
        );
    }

    // ==================== 6. Notification ====================
    public int cleanupNotifications() {
        return cleanup(
            "notification",
            cleanupProperties.getNotification(),
            orphanCleanupRepository::findOrphanNotificationIds,
            txService::cleanupNotifications
        );
    }

    // ==================== 7. Follow ====================
    public int cleanupFollows() {
        return cleanup(
            "follow",
            cleanupProperties.getFollow(),
            orphanCleanupRepository::findOrphanFollowIds,
            txService::cleanupFollows
        );
    }

    // ==================== 8. ReadStatus ====================
    public int cleanupReadStatuses() {
        return cleanup(
            "readStatus",
            cleanupProperties.getReadStatus(),
            orphanCleanupRepository::findOrphanReadStatusIds,
            txService::cleanupReadStatuses
        );
    }

    // ==================== 9. DirectMessage ====================
    public int cleanupDirectMessages() {
        return cleanup(
            "directMessage",
            cleanupProperties.getDirectMessage(),
            orphanCleanupRepository::findOrphanDirectMessageIds,
            txService::cleanupDirectMessages
        );
    }

    private int cleanup(
        String name,
        OrphanCleanupPolicyProperties policy,
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
