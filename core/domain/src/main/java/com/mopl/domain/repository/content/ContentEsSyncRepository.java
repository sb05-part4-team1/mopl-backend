package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel;
import java.time.Instant;
import java.util.List;

public interface ContentEsSyncRepository {

    List<ContentModel> findSyncTargets(Instant lastCreatedAt, String lastId, int limit);
}
