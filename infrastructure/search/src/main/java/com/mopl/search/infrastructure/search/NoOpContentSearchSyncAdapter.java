package com.mopl.search.infrastructure.search;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
