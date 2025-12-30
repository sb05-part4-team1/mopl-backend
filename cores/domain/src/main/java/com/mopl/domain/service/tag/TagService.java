package com.mopl.domain.service.tag;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagModel> findOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<TagModel> tags = tagNames.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(name -> !name.isEmpty())
            .distinct()
            .map(this::findOrCreateTag)
            .toList();

        return tagRepository.saveAll(tags);
    }

    public TagModel findOrCreateTag(String name) {
        return tagRepository.findByName(name)
            .map(tag -> {
                if (tag.isDeleted()) {
                    tag.restore();
                }
                return tag;
            })
            .orElseGet(() -> TagModel.create(name));
    }
}
