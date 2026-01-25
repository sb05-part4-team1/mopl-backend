package com.mopl.batch.cleanup.service.content;

import com.mopl.batch.cleanup.strategy.content.ContentDeletionStrategy;
import com.mopl.domain.support.search.ContentSearchSyncPort;                 // ES 동기화 포트
import com.mopl.domain.support.transaction.AfterCommitExecutor;            // 커밋 이후 실행기
import com.mopl.domain.repository.content.ContentCleanupRepository;
import com.mopl.domain.repository.content.ContentExternalMappingRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import java.time.Instant;
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
public class ContentCleanupTxService {

    private final ContentTagRepository contentTagRepository;
    private final PlaylistContentRepository playlistContentRepository;
    private final ContentCleanupRepository contentCleanupRepository;
    private final ReviewRepository reviewRepository;
    private final ContentExternalMappingRepository externalMappingRepository;
    private final ContentDeletionStrategy deletionStrategy;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    @Transactional
    public int cleanupBatch(List<UUID> contentIds) {
        Instant now = Instant.now();

        Map<UUID, String> thumbnailPaths = contentCleanupRepository.findThumbnailPathsByIdIn(contentIds);

        int deletedMappings = externalMappingRepository.deleteAllByContentIds(contentIds);

        int deletedTags = contentTagRepository.deleteAllByContentIds(contentIds);
        int deletedPlaylistContents = playlistContentRepository.deleteAllByContentIds(contentIds);

        int softDeletedReviews = reviewRepository.softDeleteByContentIds(contentIds, now);
        int affectedThumbnails = deletionStrategy.onDeleted(thumbnailPaths);

        int deletedContents = contentCleanupRepository.deleteAllByIdIn(contentIds);

        if (deletedContents != contentIds.size()) {
            log.warn(
                "content delete mismatch. requested={} deleted={}",
                contentIds.size(),
                deletedContents
            );
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.deleteAll(contentIds)
        );

        log.info(
            "content cleanup batch done. requested={} deletedContents={} deletedMappings={} deletedTags={} deletedPlaylistContents={} softDeletedReviews={} affectedThumbnails={}/{}",
            contentIds.size(),
            deletedContents,
            deletedMappings,
            deletedTags,
            deletedPlaylistContents,
            softDeletedReviews,
            affectedThumbnails,
            thumbnailPaths.size()
        );

        return deletedContents;
    }
}
