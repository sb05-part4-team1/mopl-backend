package com.mopl.domain.repository.content.batch;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ContentDeletionLogRepository {

    List<ContentDeletionLogItem> findImageCleanupTargets(int limit);

    List<UUID> findFullyProcessedLogIds(int limit);

    int saveAll(Map<UUID, String> thumbnailPathsByContentId);

    void markImageProcessed(List<UUID> logIds, Instant now);

    int deleteAllByIdIn(List<UUID> logIds);
}
