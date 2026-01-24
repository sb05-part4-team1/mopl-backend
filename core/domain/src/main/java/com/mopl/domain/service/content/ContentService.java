package com.mopl.domain.service.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;

import java.util.UUID;

@RequiredArgsConstructor
public class ContentService {

    private final ContentCacheService contentCacheService;
    private final ContentQueryRepository contentQueryRepository;
    private final ContentRepository contentRepository;

    public CursorResponse<ContentModel> getAll(ContentQueryRequest request) {
        return contentQueryRepository.findAll(request);
    }

    public ContentModel getById(UUID contentId) {
        return contentCacheService.getById(contentId);
    }

    @CacheEvict(cacheNames = CacheName.CONTENTS, key = "#result.id")
    public ContentModel create(ContentModel content) {
        return contentRepository.save(content);
    }

    public ContentModel update(ContentModel contentModel) {
        ContentModel saved = contentRepository.save(contentModel);
        contentCacheService.evict(saved.getId());
        return saved;
    }

    @CacheEvict(cacheNames = CacheName.CONTENTS, key = "#contentModel.id")
    public void delete(ContentModel contentModel) {
        contentRepository.save(contentModel);
    }
}
