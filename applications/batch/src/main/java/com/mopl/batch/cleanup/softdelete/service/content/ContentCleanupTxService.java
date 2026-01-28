package com.mopl.batch.cleanup.softdelete.service.content;

import com.mopl.batch.cleanup.softdelete.strategy.content.ContentDeletionStrategy;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentCleanupTxService {

    private final ContentCleanupRepository contentCleanupRepository;
    private final ContentDeletionStrategy deletionStrategy;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    @Transactional
    public int cleanupBatch(List<UUID> contentIds) {
        Map<UUID, String> thumbnailPaths = contentCleanupRepository.findThumbnailPathsByIdIn(contentIds);
        int affectedThumbnails = deletionStrategy.onDeleted(thumbnailPaths);

        int deletedContents = contentCleanupRepository.deleteByIdIn(contentIds);

        if (deletedContents != contentIds.size()) {
            LogContext.with("service", "contentCleanupTx")
                .and("requested", contentIds.size())
                .and("deleted", deletedContents)
                .warn("Content delete mismatch");
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.deleteAll(contentIds));

        LogContext.with("service", "contentCleanupTx")
            .and("requested", contentIds.size())
            .and("deletedContents", deletedContents)
            .and("affectedThumbnails", affectedThumbnails)
            .and("totalThumbnails", thumbnailPaths.size())
            .info("Cleanup batch completed");

        return deletedContents;
    }
}
