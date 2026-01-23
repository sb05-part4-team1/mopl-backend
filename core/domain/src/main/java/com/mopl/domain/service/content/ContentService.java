package com.mopl.domain.service.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ContentService {

    private final ContentCacheService contentCacheService;
    private final ContentTagService contentTagService;
    private final ContentQueryRepository contentQueryRepository;
    private final ContentRepository contentRepository;

    public CursorResponse<ContentModel> getAll(ContentQueryRequest request) {
        return contentQueryRepository.findAll(request);
    }

    @CacheEvict(cacheNames = CacheName.CONTENTS, key = "#result.id")
    public ContentModel create(ContentModel content) {
        return contentRepository.save(content);
    }

    public boolean exists(UUID contentId) {
        return contentRepository.existsById(contentId);
    }

    public ContentModel getById(UUID contentId) {
        return contentCacheService.getById(contentId);
    }

    public ContentModel update(
        UUID contentId,
        String title,
        String description,
        String thumbnailPath,
        List<String> tagNames
    ) {
        ContentModel content = contentCacheService.getById(contentId);

        ContentModel updated = content.update(title, description, thumbnailPath);
        ContentModel saved = contentRepository.save(updated);

        if (tagNames == null) {
            contentCacheService.evict(saved.getId());
            return saved;
        }

        contentTagService.deleteAllByContentId(saved.getId());
        contentTagService.applyTags(saved.getId(), tagNames);

        contentCacheService.evict(saved.getId());
        return saved;
    }

    public void delete(UUID contentId) {
        ContentModel content = contentCacheService.getById(contentId);
        content.delete();
        contentRepository.save(content);
        contentCacheService.evict(contentId);
    }
}
