package com.mopl.domain.repository.tag;

import com.mopl.domain.model.tag.TagModel;

import java.util.Optional;

public interface TagRepository {

    TagModel save(TagModel tagModel);

    Optional<TagModel> findByName(String tagName);
}
