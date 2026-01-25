package com.mopl.domain.repository.content.batch;

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

    int deleteAllByContentIds(List<UUID> contentIds);
}
