package com.mopl.batch.cleanup.softdelete.strategy.content;

import java.util.Map;
import java.util.UUID;

public interface ContentDeletionStrategy {

    int onDeleted(Map<UUID, String> thumbnailPathsByContentId);
}
