package com.mopl.batch.cleanup.service.content;

import com.mopl.batch.cleanup.strategy.content.ContentDeletionStrategy;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCleanupExecutor {

    private final ContentTagRepository contentTagRepository;
    private final PlaylistContentRepository playlistContentRepository;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;
    private final ContentDeletionStrategy deletionStrategy;

    @Transactional
    public int cleanupBatch(List<UUID> contentIds) {
        Map<UUID, String> thumbnailPathsByContentId = contentRepository.findThumbnailPathsByIds(
            contentIds);

        contentTagRepository.deleteAllByContentIds(contentIds);
        playlistContentRepository.deleteAllByContentIds(contentIds);

        int softDeletedReviews = reviewRepository.softDeleteByContentIds(contentIds);
        int affectedThumbnails = deletionStrategy.onDeleted(thumbnailPathsByContentId);
        int deletedContents = contentRepository.deleteAllByIds(contentIds);

        if (softDeletedReviews > 0) {
            log.info(
                "review soft-deleted by content cleanup. contentCount={} softDeletedReviews={}",
                contentIds.size(),
                softDeletedReviews
            );
        }

        if (deletedContents != contentIds.size()) {
            log.warn(
                "content delete mismatch. requested={} deleted={}",
                contentIds.size(),
                deletedContents
            );
        }

        if (affectedThumbnails != thumbnailPathsByContentId.size()) {
            log.info(
                "thumbnail handling summary. requested={} affected={}",
                thumbnailPathsByContentId.size(),
                affectedThumbnails
            );
        }

        return deletedContents;
    }
}
