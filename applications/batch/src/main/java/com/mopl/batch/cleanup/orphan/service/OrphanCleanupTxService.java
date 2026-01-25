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
    public int cleanupNotifications(List<UUID> ids) {
        return orphanCleanupRepository.deleteNotificationsByIds(ids);
    }

    @Transactional
    public int cleanupFollows(List<UUID> ids) {
        return orphanCleanupRepository.deleteFollowsByIds(ids);
    }

    @Transactional
    public int cleanupPlaylistSubscribers(List<UUID> ids) {
        return orphanCleanupRepository.deletePlaylistSubscribersByIds(ids);
    }

    @Transactional
    public int cleanupPlaylistContents(List<UUID> ids) {
        return orphanCleanupRepository.deletePlaylistContentsByIds(ids);
    }

    @Transactional
    public int cleanupPlaylists(List<UUID> ids, Instant now) {
        return orphanCleanupRepository.softDeletePlaylistsByIds(ids, now);
    }

    @Transactional
    public int cleanupReviews(List<UUID> ids, Instant now) {
        return orphanCleanupRepository.softDeleteReviewsByIds(ids, now);
    }

    @Transactional
    public int cleanupReadStatuses(List<UUID> ids) {
        return orphanCleanupRepository.deleteReadStatusesByIds(ids);
    }

    @Transactional
    public int cleanupDirectMessages(List<UUID> ids) {
        return orphanCleanupRepository.deleteDirectMessagesByIds(ids);
    }

    @Transactional
    public int cleanupContentTags(List<UUID> ids) {
        return orphanCleanupRepository.deleteContentTagsByIds(ids);
    }
}
