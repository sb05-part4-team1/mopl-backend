package com.mopl.search.infrastructure.search;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.search.content.service.ContentIndexService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mopl.search", name = "enabled", havingValue = "true")
public class ElasticsearchContentSearchSyncAdapter implements ContentSearchSyncPort {

    private final ContentIndexService indexService;

    @Override
    public void upsert(ContentModel model) {
        indexService.upsert(model);
    }

    @Override
    public void upsertAll(List<ContentModel> models) {
        indexService.upsertAll(models);
    }

    @Override
    public void delete(UUID contentId) {
        indexService.delete(contentId);
    }

    @Override
    public void deleteAll(List<UUID> contentIds) {
        indexService.deleteAll(contentIds);
    }
}
