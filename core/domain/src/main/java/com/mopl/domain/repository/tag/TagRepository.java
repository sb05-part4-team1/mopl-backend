package com.mopl.domain.repository.tag;

import com.mopl.domain.model.tag.TagModel;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    Optional<TagModel> findByName(String tagName);

    List<TagModel> findByNameIn(List<String> tagNames);

    List<TagModel> saveAll(List<TagModel> tags);

    TagModel save(TagModel tagModel);
}
