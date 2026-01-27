package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.jpa.entity.content.ContentDeletionLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentDeletionLogRepositoryImpl implements ContentDeletionLogRepository {

    private final JpaContentDeletionLogRepository jpaContentDeletionLogRepository;

    @Override
    public int saveAll(Map<UUID, String> thumbnailPathsByContentId) {
        if (thumbnailPathsByContentId == null || thumbnailPathsByContentId.isEmpty()) {
            return 0;
        }

        List<UUID> contentIds = thumbnailPathsByContentId.keySet().stream().toList();
        Set<UUID> existingIdSet = new HashSet<>(
            jpaContentDeletionLogRepository.findExistingContentIds(contentIds)
        );

        List<ContentDeletionLogEntity> entities = contentIds.stream()
            .filter(contentId -> !existingIdSet.contains(contentId))
            .<ContentDeletionLogEntity>map(contentId -> ContentDeletionLogEntity.builder()
                .contentId(contentId)
                .thumbnailPath(thumbnailPathsByContentId.get(contentId))
                .build())
            .toList();

        if (entities.isEmpty()) {
            return 0;
        }

        jpaContentDeletionLogRepository.saveAll(entities);
        return entities.size();
    }

    @Override
    public List<ContentDeletionLogItem> findImageCleanupTargets(int limit) {
        return jpaContentDeletionLogRepository.findImageCleanupTargets(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "deletedAt"))
            ).stream()
            .map(row -> new ContentDeletionLogItem(
                row.getLogId(),
                row.getContentId(),
                row.getThumbnailPath()
            ))
            .toList();
    }

    @Override
    public void markImageProcessed(List<UUID> logIds, Instant now) {
        if (logIds == null || logIds.isEmpty()) {
            return;
        }
        jpaContentDeletionLogRepository.markImageProcessed(logIds, now);
    }

    @Override
    public List<UUID> findFullyProcessedLogIds(int limit) {
        return jpaContentDeletionLogRepository.findFullyProcessedLogIds(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "deletedAt"))
        );
    }

    @Override
    public int deleteByIdIn(List<UUID> logIds) {
        if (logIds == null || logIds.isEmpty()) {
            return 0;
        }
        return jpaContentDeletionLogRepository.deleteByIdIn(logIds);
    }
}
