package com.mopl.api.config;

import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionQueryRepository;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.playlist.PlaylistCacheService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
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

    @Bean
    public ConversationService conversationService(
        ConversationQueryRepository conversationQueryRepository,
        ConversationRepository conversationRepository,
        DirectMessageQueryRepository directMessageQueryRepository,
        DirectMessageRepository directMessageRepository,
        ReadStatusRepository readStatusRepository,
        UserRepository userRepository
    ) {
        return new ConversationService(
            conversationQueryRepository,
            conversationRepository,
            directMessageQueryRepository,
            directMessageRepository,
            readStatusRepository,
            userRepository
        );
    }

    @Bean
    public FollowService followService(FollowRepository followRepository) {
        return new FollowService(followRepository);
    }

    @Bean
    public NotificationService notificationService(
        NotificationQueryRepository notificationQueryRepository,
        NotificationRepository notificationRepository
    ) {
        return new NotificationService(notificationQueryRepository, notificationRepository);
    }

    @Bean
    public OutboxService outboxService(OutboxRepository outboxRepository) {
        return new OutboxService(outboxRepository);
    }

    @Bean
    public PlaylistCacheService playlistCacheService(
        PlaylistRepository playlistRepository,
        PlaylistContentRepository playlistContentRepository
    ) {
        return new PlaylistCacheService(playlistRepository, playlistContentRepository);
    }

    @Bean
    public PlaylistService playlistService(
        PlaylistCacheService playlistCacheService,
        PlaylistQueryRepository playlistQueryRepository,
        PlaylistContentRepository playlistContentRepository
    ) {
        return new PlaylistService(
            playlistCacheService,
            playlistQueryRepository,
            playlistContentRepository
        );
    }

    @Bean
    public PlaylistSubscriptionService playlistSubscriptionService(
        PlaylistSubscriberRepository playlistSubscriberRepository,
        PlaylistRepository playlistRepository
    ) {
        return new PlaylistSubscriptionService(playlistSubscriberRepository, playlistRepository);
    }

    @Bean
    public ReviewService reviewService(
        ReviewQueryRepository reviewQueryRepository,
        ReviewRepository reviewRepository,
        ContentRepository contentRepository
    ) {
        return new ReviewService(reviewQueryRepository, reviewRepository, contentRepository);
    }

    @Bean
    public UserService userService(
        UserQueryRepository userQueryRepository,
        UserRepository userRepository
    ) {
        return new UserService(userQueryRepository, userRepository);
    }

    @Bean
    public WatchingSessionService watchingSessionService(
        WatchingSessionQueryRepository watchingSessionQueryRepository,
        WatchingSessionRepository watchingSessionRepository
    ) {
        return new WatchingSessionService(
            watchingSessionQueryRepository,
            watchingSessionRepository
        );
    }
}
