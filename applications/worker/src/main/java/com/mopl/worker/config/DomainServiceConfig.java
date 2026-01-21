package com.mopl.worker.config;

import com.mopl.domain.repository.follow.FollowRepository;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.service.follow.FollowService;
import com.mopl.domain.service.notification.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public NotificationService notificationService(
        NotificationRepository notificationRepository,
        NotificationQueryRepository notificationQueryRepository
    ) {
        return new NotificationService(notificationRepository, notificationQueryRepository);
    }

    @Bean
    public FollowService followService(FollowRepository followRepository) {
        return new FollowService(followRepository);
    }
}
