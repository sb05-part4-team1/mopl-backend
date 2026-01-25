package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.jpa.entity.content.ContentDeletionLogEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentDeletionLogRepositoryImpl implements ContentDeletionLogRepository {

    private final JpaContentDeletionLogRepository jpaContentDeletionLogRepository;

    @Override
    public int saveAll(Map<UUID, String> thumbnailPathsByContentId) {
        if (thumbnailPathsByContentId == null || thumbnailPathsByContentId.isEmpty()) {
            return 0;
        }

        List<UUID> contentIds = new ArrayList<>(thumbnailPathsByContentId.keySet());
        List<UUID> existingIds = jpaContentDeletionLogRepository.findExistingContentIds(contentIds);
        Set<UUID> existingIdSet = new HashSet<>(existingIds);

        List<ContentDeletionLogEntity> entities = new ArrayList<>();

        for (UUID contentId : contentIds) {
            if (existingIdSet.contains(contentId)) {
                continue;
            }

            String thumbnailPath = thumbnailPathsByContentId.get(contentId);

            ContentDeletionLogEntity entity = ContentDeletionLogEntity.builder()
                .contentId(contentId)
                .thumbnailPath(thumbnailPath)
                .build();

            entities.add(entity);
        }

        if (entities.isEmpty()) {
            return 0;
        }

        jpaContentDeletionLogRepository.saveAll(entities);
        return entities.size();
    }

    @Override
    public List<ContentDeletionLogItem> findImageCleanupTargets(int limit) {
        Pageable pageable = PageRequest.of(
            0,
            limit,
            Sort.by(Sort.Direction.ASC, "deletedAt")
        );

        return jpaContentDeletionLogRepository.findImageCleanupTargets(pageable).stream()
            .map(row -> new ContentDeletionLogItem(
                row.getLogId(),
                row.getContentId(),
                row.getThumbnailPath()
            ))
            .collect(Collectors.toList());
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
        Pageable pageable = PageRequest.of(
            0,
            limit,
            Sort.by(Sort.Direction.ASC, "deletedAt")
        );

        return jpaContentDeletionLogRepository.findFullyProcessedLogIds(pageable);
    }

    @Override
    public int deleteAllByIdIn(List<UUID> logIds) {
        if (logIds == null || logIds.isEmpty()) {
            return 0;
        }
        return jpaContentDeletionLogRepository.deleteAllByIds(logIds);
    }
}
