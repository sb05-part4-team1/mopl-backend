package com.mopl.domain.service.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final TagRepository tagRepository;

    // ===== 조회 (Query) =====

    public List<String> getTagNamesByContentId(UUID contentId) {
        return toTagNames(contentTagRepository.findTagsByContentId(contentId));
    }

    public Map<UUID, List<String>> getTagNamesByContentIdIn(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }

        return contentTagRepository.findTagsByContentIdIn(contentIds)
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toTagNames(entry.getValue())
            ));
    }

    // ===== 명령 (Command) =====

    public void applyTags(UUID contentId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        List<TagModel> tags = findOrCreateTags(tagNames);
        contentTagRepository.saveAll(contentId, tags);
    }

    public void deleteAllByContentId(UUID contentId) {
        contentTagRepository.deleteAllByContentId(contentId);
    }

    // ===== private =====

    private List<TagModel> findOrCreateTags(List<String> tagNames) {
        List<TagModel> tags = tagNames.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(name -> !name.isEmpty())
            .distinct()
            .map(this::findOrCreateTag)
            .toList();

        return tagRepository.saveAll(tags);
    }

    private TagModel findOrCreateTag(String name) {
        return tagRepository.findByName(name)
            .map(tag -> {
                if (tag.isDeleted()) {
                    tag.restore();
                }
                return tag;
            })
            .orElseGet(() -> TagModel.create(name));
    }

    private List<String> toTagNames(List<TagModel> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
            .map(TagModel::getName)
            .toList();
    }
}
