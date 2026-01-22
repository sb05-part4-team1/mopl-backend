package com.mopl.batch.collect.tmdb.config;

import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.tag.TagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ContentService contentService(
        TagService tagService,
        ContentRepository contentRepository,
        ContentQueryRepository contentQueryRepository,
        ContentTagRepository contentTagRepository
    ) {
        return new ContentService(
            tagService,
            contentRepository,
            contentQueryRepository,
            contentTagRepository
        );
    }

    @Bean
    public TagService tagService(TagRepository tagRepository) {
        return new TagService(tagRepository);
    }
}
