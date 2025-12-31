package com.mopl.domain.repository.tag;

import com.mopl.domain.model.tag.TagModel;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    TagModel save(TagModel tagModel);

    List<TagModel> saveAll(List<TagModel> tags);

    Optional<TagModel> findByName(String tagName);
}
