package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.repository.content.batch.ContentCleanupRepository;
import com.mopl.jpa.repository.content.JpaContentRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentCleanupRepositoryImpl implements ContentCleanupRepository {

    private final JpaContentRepository jpaContentRepository;

    @Override
    public List<UUID> findCleanupTargets(Instant threshold, int limit) {
        return jpaContentRepository.findCleanupTargets(threshold, limit);
    }

    @Override
    public Map<UUID, String> findThumbnailPathsByIdIn(List<UUID> contentIds) {
        List<ContentThumbnailRow> rows = jpaContentRepository.findThumbnailPathsByIds(contentIds);

        Map<UUID, String> result = new HashMap<>();
        for (ContentThumbnailRow row : rows) {
            result.put(row.getId(), row.getThumbnailPath());
        }
        return result;
    }

    @Override
    public int deleteByIdIn(List<UUID> contentIds) {
        return jpaContentRepository.deleteByIdIn(contentIds);
    }
}
