package com.mopl.batch.collect.tmdb.config;

import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.service.content.ContentCacheService;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.tag.TagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ContentService contentService(
        ContentCacheService contentCacheService,
        ContentTagService contentTagService,
        ContentRepository contentRepository,
        ContentQueryRepository contentQueryRepository
    ) {
        return new ContentService(
            contentCacheService,
            contentTagService,
            contentQueryRepository,
            contentRepository
        );
    }

    @Bean
    ContentCacheService contentCacheService(
        ContentRepository contentRepository,
        ContentTagRepository contentTagRepository
    ) {
        return new ContentCacheService(
            contentRepository,
            contentTagRepository
        );
    }

    @Bean
    public TagService tagService(TagRepository tagRepository) {
        return new TagService(tagRepository);
    }
}
