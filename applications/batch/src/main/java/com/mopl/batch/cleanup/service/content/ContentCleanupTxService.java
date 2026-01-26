package com.mopl.batch.cleanup.service.content;

import com.mopl.batch.cleanup.strategy.content.ContentDeletionStrategy;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.domain.repository.content.batch.ContentExternalMappingRepository;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCleanupTxService {

    private final ContentTagRepository contentTagRepository;
    private final ContentCleanupRepository contentCleanupRepository;
    private final ContentExternalMappingRepository externalMappingRepository;
    private final ContentDeletionStrategy deletionStrategy;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    @Transactional
    public int cleanupBatch(List<UUID> contentIds) {
        Map<UUID, String> thumbnailPaths = contentCleanupRepository.findThumbnailPathsByIdIn(contentIds);

        int deletedMappings = externalMappingRepository.deleteAllByContentIds(contentIds);
        int deletedTags = contentTagRepository.deleteByContentIdIn(contentIds);
        int affectedThumbnails = deletionStrategy.onDeleted(thumbnailPaths);

        int deletedContents = contentCleanupRepository.deleteByIdIn(contentIds);

        if (deletedContents != contentIds.size()) {
            log.warn(
                "content delete mismatch. requested={} deleted={}",
                contentIds.size(),
                deletedContents
            );
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.deleteAll(contentIds));

        log.info(
            "content cleanup batch done. requested={} deletedContents={} deletedMappings={} deletedTags={} affectedThumbnails={}/{}",
            contentIds.size(),
            deletedContents,
            deletedMappings,
            deletedTags,
            affectedThumbnails,
            thumbnailPaths.size()
        );

        return deletedContents;
    }
}
