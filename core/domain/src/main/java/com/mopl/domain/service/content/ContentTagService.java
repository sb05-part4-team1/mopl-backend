package com.mopl.domain.service.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.support.cache.CacheName;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContentTagService {

    private final ContentTagRepository contentTagRepository;
    private final TagRepository tagRepository;

    @Cacheable(cacheNames = CacheName.CONTENT_TAGS, key = "#contentId")
    public List<TagModel> getTagsByContentId(UUID contentId) {
        return contentTagRepository.findTagsByContentId(contentId);
    }

    public Map<UUID, List<TagModel>> getTagsByContentIdIn(List<UUID> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }
        return contentTagRepository.findTagsByContentIdIn(contentIds);
    }

    @CacheEvict(cacheNames = CacheName.CONTENT_TAGS, key = "#contentId")
    public void applyTags(UUID contentId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        Set<String> normalizedNames = normalizeTagNames(tagNames);
        if (normalizedNames.isEmpty()) {
            return;
        }

        Map<String, TagModel> existingByName = tagRepository.findByNameIn(normalizedNames).stream()
            .collect(Collectors.toMap(TagModel::getName, Function.identity()));

        List<TagModel> tagsToSave = normalizedNames.stream()
            .map(name -> existingByName.getOrDefault(name, TagModel.create(name)))
            .toList();

        List<TagModel> savedTags = tagRepository.saveAll(tagsToSave);
        contentTagRepository.saveAll(contentId, savedTags);
    }

    @CacheEvict(cacheNames = CacheName.CONTENT_TAGS, key = "#contentId")
    public void deleteAllByContentId(UUID contentId) {
        contentTagRepository.deleteByContentId(contentId);
    }

    private Set<String> normalizeTagNames(List<String> tagNames) {
        return tagNames.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(name -> !name.isEmpty())
            .collect(Collectors.toSet());
    }
}
