package com.mopl.domain.service.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final TagRepository tagRepository;

    public List<TagModel> getTagsByContentId(UUID contentId) {
        return contentTagRepository.findTagsByContentId(contentId);
    }

    public Map<UUID, List<TagModel>> getTagsByContentIdIn(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }
        return contentTagRepository.findTagsByContentIdIn(contentIds);
    }

    public void applyTags(UUID contentId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        List<String> normalizedNames = normalizeTagNames(tagNames);
        if (normalizedNames.isEmpty()) {
            return;
        }

        List<TagModel> existingTags = tagRepository.findByNameIn(normalizedNames);
        List<TagModel> tagsToSave = resolveOrCreateTags(normalizedNames, existingTags);

        List<TagModel> savedTags = tagRepository.saveAll(tagsToSave);
        contentTagRepository.saveAll(contentId, savedTags);
    }

    public void deleteAllByContentId(UUID contentId) {
        contentTagRepository.deleteAllByContentId(contentId);
    }

    private List<String> normalizeTagNames(List<String> tagNames) {
        return tagNames.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(name -> !name.isEmpty())
            .distinct()
            .toList();
    }

    private List<TagModel> resolveOrCreateTags(List<String> names, List<TagModel> existingTags) {
        Map<String, TagModel> existingByName = existingTags.stream()
            .collect(Collectors.toMap(TagModel::getName, Function.identity()));

        return names.stream()
            .map(name -> {
                TagModel existing = existingByName.get(name);
                if (existing != null) {
                    if (existing.isDeleted()) {
                        existing.restore();
                    }
                    return existing;
                }
                return TagModel.create(name);
            })
            .toList();
    }
}
