package com.mopl.search.content.sync;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpContentSearchSyncAdapter implements ContentSearchSyncPort {

    @Override
    public void upsert(ContentModel model) {
    }

    @Override
    public void upsertAll(List<ContentModel> models) {
    }

    @Override
    public void delete(UUID contentId) {
    }

    @Override
    public void deleteAll(List<UUID> contentIds) {
    }
}
