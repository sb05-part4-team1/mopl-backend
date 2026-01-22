package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentExternalProvider;
import java.util.List;
import java.util.UUID;

public interface ContentExternalMappingRepository {

    void save(
        ContentExternalProvider provider,
        Long externalId,
        UUID contentId
    );

    boolean exists(
        ContentExternalProvider provider,
        Long externalId
    );

    // 이하 메서드들 cleanup batch 전용
    int deleteAllByContentIds(List<UUID> contentIds);
}
