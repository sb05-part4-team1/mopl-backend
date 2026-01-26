package com.mopl.batch.cleanup.orphan.service;

import com.mopl.batch.sync.denormalized.service.ContentReviewStatsSyncTxService;
import com.mopl.jpa.repository.orphan.JpaOrphanCleanupRepository;
import com.mopl.jpa.repository.review.JpaReviewRepository;
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
    private final JpaReviewRepository jpaReviewRepository;
    private final ContentReviewStatsSyncTxService contentReviewStatsSyncTxService;

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
    public int cleanupPlaylists(List<UUID> playlistIds) {
        return orphanCleanupRepository.deletePlaylistsByIdIn(playlistIds);
    }

    @Transactional
    public int cleanupReviews(List<UUID> reviewIds) {
        // 1. 삭제 전 영향받는 contentId 조회
        Set<UUID> contentIds = jpaReviewRepository.findContentIdsByIdIn(reviewIds);

        // 2. Review hard delete
        int deleted = orphanCleanupRepository.deleteReviewsByIdIn(reviewIds);

        // 3. Content 통계 재계산
        contentIds.forEach(contentReviewStatsSyncTxService::syncOne);

        return deleted;
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
