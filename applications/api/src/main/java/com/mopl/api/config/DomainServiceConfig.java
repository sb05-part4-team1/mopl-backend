package com.mopl.api.config;

import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.repository.user.FollowRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.tag.TagService;
import com.mopl.domain.service.user.FollowService;
import com.mopl.domain.service.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public FollowService followService(FollowRepository followRepository) {
        return new FollowService(followRepository);
    }

    @Bean
    public ContentService contentService(
        ContentRepository contentRepository,
        ContentTagRepository contentTagRepository
    ) {
        return new ContentService(contentRepository, contentTagRepository);
    }

    @Bean
    public TagService tagService(TagRepository tagRepository) {
        return new TagService(tagRepository);
    }

    @Bean
    public ReviewService reviewService(
        ReviewRepository reviewRepository,
        ContentRepository contentRepository
    ) {
        return new ReviewService(
            reviewRepository,
            contentRepository
        );
    }
}
