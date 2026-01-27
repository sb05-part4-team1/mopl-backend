package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.query.ContentQueryRepository;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.UUID;

@RequiredArgsConstructor
public class ContentService {

    private final ContentQueryRepository contentQueryRepository;
    private final ContentRepository contentRepository;

    public CursorResponse<ContentModel> getAll(ContentQueryRequest request) {
        return contentQueryRepository.findAll(request);
    }

    @Cacheable(cacheNames = CacheName.CONTENTS, key = "#contentId")
    public ContentModel getById(UUID contentId) {
        return contentRepository.findById(contentId)
            .orElseThrow(() -> ContentNotFoundException.withId(contentId));
    }

    @CachePut(cacheNames = CacheName.CONTENTS, key = "#result.id")
    public ContentModel create(ContentModel content) {
        return contentRepository.save(content);
    }

    @CachePut(cacheNames = CacheName.CONTENTS, key = "#result.id")
    public ContentModel update(ContentModel contentModel) {
        return contentRepository.save(contentModel);
    }

    @CacheEvict(cacheNames = CacheName.CONTENTS, key = "#contentModel.id")
    public void delete(ContentModel contentModel) {
        contentRepository.save(contentModel);
    }
}
