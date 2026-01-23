package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.service.tag.TagService;
import com.mopl.domain.support.cursor.CursorResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentService {

    private final ContentCacheService contentCacheService;
    private final TagService tagService;
    private final ContentRepository contentRepository;
    private final ContentQueryRepository contentQueryRepository;
    private final ContentTagRepository contentTagRepository;

    public ContentModel create(ContentModel content, List<String> tagNames) {
        ContentModel savedContent = contentRepository.save(content);
        ContentModel savedWithTags = applyTags(savedContent, tagNames);
        contentCacheService.evict(savedWithTags.getId());
        return savedWithTags;
    }

    public boolean exists(UUID contentId) {
        return contentRepository.existsById(contentId);
    }

    public CursorResponse<ContentModel> getAll(ContentQueryRequest request) {
        return contentQueryRepository.findAll(request);
    }

    public ContentModel getById(UUID contentId) {
        return contentCacheService.getById(contentId);
    }

    public ContentModel update(
        UUID contentId,
        String title,
        String description,
        String thumbnailUrl,
        List<String> tagNames
    ) {
        ContentModel content = contentCacheService.getById(contentId);

        String finalTitle = title != null ? title : content.getTitle();
        String finalDescription = description != null ? description : content.getDescription();
        String finalThumbnailUrl = thumbnailUrl != null ? thumbnailUrl : content.getThumbnailUrl();

        ContentModel updated = content.update(finalTitle, finalDescription, finalThumbnailUrl);
        ContentModel saved = contentRepository.save(updated);

        if (tagNames == null) {
            contentCacheService.evict(saved.getId());
            return saved;
        }

        contentTagRepository.deleteAllByContentId(saved.getId());
        ContentModel savedWithTags = applyTags(saved, tagNames);

        contentCacheService.evict(savedWithTags.getId());
        return savedWithTags;
    }

    public void delete(UUID contentId) {
        ContentModel content = contentCacheService.getById(contentId);
        content.delete();
        contentRepository.save(content);
        contentCacheService.evict(contentId);
    }

    private ContentModel applyTags(ContentModel content, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return content.withTags(List.of());
        }

        List<TagModel> tags = tagService.findOrCreateTags(tagNames);
        contentTagRepository.saveAll(content.getId(), tags);

        return content.withTags(toTagNames(tags));
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
