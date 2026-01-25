package com.mopl.domain.support.search;

import com.mopl.domain.model.content.ContentModel;
import java.util.List;
import java.util.UUID;

public interface ContentSearchSyncPort {

    void upsert(ContentModel model);

    void upsertAll(List<ContentModel> models);

    void delete(UUID contentId);

    void deleteAll(List<UUID> contentIds);
}
