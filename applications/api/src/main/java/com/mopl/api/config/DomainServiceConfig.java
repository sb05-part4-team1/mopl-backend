package com.mopl.api.config;

import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.tag.TagService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public UserService userService(
        UserRepository userRepository,
        UserQueryRepository userQueryRepository
    ) {
        return new UserService(userRepository, userQueryRepository);
    }

    @Bean
    public FollowService followService(FollowRepository followRepository) {
        return new FollowService(followRepository);
    }

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

    @Bean
    public PlaylistService playlistService(
        PlaylistRepository playlistRepository,
        PlaylistContentRepository playlistContentRepository
    ) {
        return new PlaylistService(
            playlistRepository,
            playlistContentRepository
        );
    }

    @Bean
    public PlaylistSubscriptionService playlistSubscriptionService(
        PlaylistSubscriberRepository playlistSubscriberRepository
    ) {
        return new PlaylistSubscriptionService(playlistSubscriberRepository);
    }

    @Bean
    public NotificationService notificationService(
        NotificationRepository notificationRepository
    ) {
        return new NotificationService(
            notificationRepository
        );
    }

    @Bean
    public WatchingSessionService watchingSessionService(
        WatchingSessionRepository watchingSessionRepository,
        WatchingSessionQueryRepository watchingSessionQueryRepository
    ) {
        return new WatchingSessionService(
            watchingSessionRepository,
            watchingSessionQueryRepository
        );
    }
}
