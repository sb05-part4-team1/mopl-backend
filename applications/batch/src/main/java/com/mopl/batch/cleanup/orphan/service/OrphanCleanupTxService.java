package com.mopl.batch.cleanup.orphan.service;

import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrphanCleanupTxService {

    private final JpaOrphanCleanupRepository orphanCleanupRepository;

    @Transactional
    public int cleanupNotifications(List<UUID> notificationIds) {
        return orphanCleanupRepository.deleteNotificationsByIdIn(notificationIds);
    }

    @Transactional
    public int cleanupFollows(List<UUID> followIds) {
        return orphanCleanupRepository.deleteFollowsByIdIn(followIds);
    }

    @Transactional
    public int cleanupPlaylistSubscribers(List<UUID> playlistSubscribersByIds) {
        return orphanCleanupRepository.deletePlaylistSubscribersByIdIn(playlistSubscribersByIds);
    }

    @Transactional
    public int cleanupPlaylistContents(List<UUID> playlistContentIds) {
        return orphanCleanupRepository.deletePlaylistContentsByIdIn(playlistContentIds);
    }

    @Transactional
    public int cleanupPlaylists(List<UUID> playlistIds, Instant now) {
        return orphanCleanupRepository.softDeletePlaylistsByIdIn(playlistIds, now);
    }

    @Transactional
    public int cleanupReviews(List<UUID> reviewIds, Instant now) {
        return orphanCleanupRepository.softDeleteReviewsByIdIn(reviewIds, now);
    }

    @Transactional
    public int cleanupReadStatuses(List<UUID> readStatusIds) {
        return orphanCleanupRepository.deleteReadStatusesByIdIn(readStatusIds);
    }

    @Transactional
    public int cleanupDirectMessages(List<UUID> directMessageIds) {
        return orphanCleanupRepository.deleteDirectMessagesByIdIn(directMessageIds);
    }

    @Transactional
    public int cleanupContentTags(List<UUID> contentTagIds) {
        return orphanCleanupRepository.deleteContentTagsByIdIn(contentTagIds);
    }
}
