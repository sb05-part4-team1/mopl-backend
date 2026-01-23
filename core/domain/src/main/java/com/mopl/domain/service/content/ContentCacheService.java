package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.support.cache.CacheName;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@RequiredArgsConstructor
public class ContentCacheService {

    private final ContentRepository contentRepository;
    private final ContentTagRepository contentTagRepository;

    @Cacheable(cacheNames = CacheName.CONTENTS, key = "#contentId")
    public ContentModel getById(UUID contentId) {
        return contentRepository.findById(contentId)
            .orElseThrow(() -> ContentNotFoundException.withId(contentId));
    }

    @Cacheable(cacheNames = CacheName.CONTENT_TAGS, key = "#contentId")
    public List<TagModel> getTagsByContentId(UUID contentId) {
        return contentTagRepository.findTagsByContentId(contentId);
    }

    @CacheEvict(cacheNames = CacheName.CONTENTS, key = "#contentId")
    public void evict(UUID contentId) {
    }
}
