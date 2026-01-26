package com.mopl.domain.repository.content.batch;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ContentCleanupRepository {

    List<UUID> findCleanupTargets(Instant threshold, int limit);

    Map<UUID, String> findThumbnailPathsByIdIn(List<UUID> contentIds);

    int deleteByIdIn(List<UUID> contentIds);
}
