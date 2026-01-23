package com.mopl.domain.repository.content;

import com.mopl.domain.repository.content.dto.ContentDeletionLogItem;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ContentDeletionLogRepository {

    int saveAll(Map<UUID, String> thumbnailPathsByContentId);

    List<ContentDeletionLogItem> findImageCleanupTargets(int limit);

    void markImageProcessed(List<UUID> logIds, Instant now);

    List<UUID> findFullyProcessedLogIds(int limit);

    int deleteAllByIds(List<UUID> logIds);
}
