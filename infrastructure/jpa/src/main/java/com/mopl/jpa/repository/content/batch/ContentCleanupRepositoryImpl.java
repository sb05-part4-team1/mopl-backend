package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.jpa.repository.softdelete.JpaSoftDeleteCleanupRepository;
import com.mopl.jpa.repository.softdelete.projection.ContentThumbnailProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentCleanupRepositoryImpl implements ContentCleanupRepository {

    private final JpaSoftDeleteCleanupRepository softDeleteCleanupRepository;

    @Override
    public List<UUID> findCleanupTargets(Instant threshold, int limit) {
        return softDeleteCleanupRepository.findCleanupTargets(threshold, limit);
    }

    @Override
    public Map<UUID, String> findThumbnailPathsByIdIn(List<UUID> contentIds) {
        List<ContentThumbnailProjection> rows = softDeleteCleanupRepository.findThumbnailPathsByIds(contentIds);

        Map<UUID, String> result = new HashMap<>();
        for (ContentThumbnailProjection row : rows) {
            result.put(row.getId(), row.getThumbnailPath());
        }
        return result;
    }

    @Override
    public int deleteByIdIn(List<UUID> contentIds) {
        return softDeleteCleanupRepository.deleteContentsByIdIn(contentIds);
    }
}
