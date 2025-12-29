package com.mopl.domain.repository.content;

import com.mopl.domain.model.tag.TagModel;

import java.util.List;
import java.util.UUID;

public interface ContentTagRepository {

    void saveAll(UUID contentId, List<TagModel> tags);
}
