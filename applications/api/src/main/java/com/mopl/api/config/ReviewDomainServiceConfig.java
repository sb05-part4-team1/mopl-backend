package com.mopl.api.config;

import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.service.review.ReviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewDomainServiceConfig {

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
