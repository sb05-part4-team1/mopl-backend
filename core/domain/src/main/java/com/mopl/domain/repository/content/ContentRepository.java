package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ContentRepository {

    ContentModel save(ContentModel contentModel);

    boolean existsById(UUID contentId);

    Optional<ContentModel> findById(UUID contentId);

    // 이하 메서드들 cleanup batch 전용
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    Map<UUID, String> findThumbnailPathsByIds(List<UUID> contentIds);

    int deleteAllByIds(List<UUID> contentIds);
}
