package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentTagRepository contentTagRepository;

    public ContentModel create(ContentModel content, List<TagModel> tags) {
        ContentModel savedContent = contentRepository.save(content);

        if (tags != null && !tags.isEmpty()) {
            contentTagRepository.saveAll(savedContent.getId(), tags);
        }

        return savedContent.withTags(toTagNames(tags));
    }

    public boolean exists(UUID contentId) {
        return contentRepository.existsById(contentId);
    }

    public ContentModel getById(UUID contentId) {
        ContentModel content = contentRepository.findById(contentId)
            .orElseThrow(() -> ContentNotFoundException.withId(contentId));

        List<TagModel> tags = contentTagRepository.findTagsByContentId(contentId);

        return content.withTags(toTagNames(tags));
    }

    private List<String> toTagNames(List<TagModel> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream().map(TagModel::getName).toList();
    }
}
