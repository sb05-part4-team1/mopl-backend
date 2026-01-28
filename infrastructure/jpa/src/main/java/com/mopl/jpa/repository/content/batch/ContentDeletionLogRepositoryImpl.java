package com.mopl.jpa.repository.content.batch;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.mopl.domain.repository.content.batch.ContentDeletionLogItem;
import com.mopl.domain.repository.content.batch.ContentDeletionLogRepository;
import com.mopl.jpa.support.batch.JdbcBatchInsertHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

    private static final TimeBasedEpochGenerator UUID_GENERATOR = Generators.timeBasedEpochGenerator();

    private static final String BATCH_INSERT_SQL = """
        INSERT INTO content_deletion_logs (id, content_id, thumbnail_path, created_at)
        VALUES (:id, :contentId, :thumbnailPath, :createdAt)
        """;

    private final JpaContentDeletionLogRepository jpaContentDeletionLogRepository;
    private final JdbcBatchInsertHelper jdbcBatchInsertHelper;

    @Override
    public int saveAll(Map<UUID, String> thumbnailPathsByContentId) {
        if (thumbnailPathsByContentId == null || thumbnailPathsByContentId.isEmpty()) {
            return 0;
        }

        List<UUID> contentIds = thumbnailPathsByContentId.keySet().stream().toList();
        Set<UUID> existingIdSet = new HashSet<>(
            jpaContentDeletionLogRepository.findExistingContentIds(contentIds)
        );

        List<UUID> newContentIds = contentIds.stream()
            .filter(contentId -> !existingIdSet.contains(contentId))
            .toList();

        if (newContentIds.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
        jdbcBatchInsertHelper.batchInsert(
            BATCH_INSERT_SQL,
            newContentIds,
            contentId -> toParameterSource(contentId, thumbnailPathsByContentId.get(contentId), now)
        );

        return newContentIds.size();
    }

    private MapSqlParameterSource toParameterSource(UUID contentId, String thumbnailPath, Instant createdAt) {
        return new MapSqlParameterSource()
            .addValue("id", uuidToBytes(UUID_GENERATOR.generate()))
            .addValue("contentId", uuidToBytes(contentId))
            .addValue("thumbnailPath", thumbnailPath)
            .addValue("createdAt", createdAt);
    }

    private byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[i + 8] = (byte) (lsb >>> (8 * (7 - i)));
        }
        return bytes;
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
