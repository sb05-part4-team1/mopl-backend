package com.mopl.batch.collect.tmdb.config;

import com.mopl.domain.repository.content.query.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ContentService contentService(
        ContentQueryRepository contentQueryRepository,
        ContentRepository contentRepository
    ) {
        return new ContentService(contentQueryRepository, contentRepository);
    }

    @Bean
    public ContentTagService contentTagService(
        ContentTagRepository contentTagRepository,
        TagRepository tagRepository
    ) {
        return new ContentTagService(contentTagRepository, tagRepository);
    }
}
