package com.mopl.domain.repository.content;

import com.mopl.domain.model.tag.TagModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ContentTagRepository {

    List<TagModel> findTagsByContentId(UUID contentId);

    Map<UUID, List<TagModel>> findTagsByContentIds(List<UUID> contentIds);

    void saveAll(UUID contentId, List<TagModel> tags);

    void deleteAllByContentId(UUID contentId);
}
