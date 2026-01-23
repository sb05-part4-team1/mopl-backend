package com.mopl.domain.service.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.service.tag.TagService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final TagService tagService;

    public List<String> getTagNamesByContentId(UUID contentId) {
        return toTagNames(contentTagRepository.findTagsByContentId(contentId));
    }

    public Map<UUID, List<String>> getTagNamesByContentIds(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<TagModel>> tagsByContentId = contentTagRepository.findTagsByContentIds(contentIds);

        return tagsByContentId.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toTagNames(entry.getValue())
            ));
    }

    public void applyTags(UUID contentId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        List<TagModel> tags = tagService.findOrCreateTags(tagNames);
        contentTagRepository.saveAll(contentId, tags);
    }

    public void deleteAllByContentId(UUID contentId) {
        contentTagRepository.deleteAllByContentId(contentId);
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
