package com.mopl.worker.config;

import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

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
    public PlaylistSubscriptionService playlistSubscriptionService(
        PlaylistSubscriberRepository playlistSubscriberRepository,
        PlaylistRepository playlistRepository
    ) {
        return new PlaylistSubscriptionService(playlistSubscriberRepository, playlistRepository);
    }
}
