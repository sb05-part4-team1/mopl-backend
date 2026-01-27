package com.mopl.domain.repository.tag;

import com.mopl.domain.model.tag.TagModel;

import java.util.Collection;
import java.util.List;

public interface TagRepository {

    List<TagModel> findByNameIn(Collection<String> tagNames);

    List<TagModel> saveAll(List<TagModel> tags);
}
