package com.mopl.worker.config;

import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.notification.NotificationService;
import com.mopl.domain.service.user.UserService;
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
    public NotificationService notificationService(
        NotificationRepository notificationRepository,
        NotificationQueryRepository notificationQueryRepository
    ) {
        return new NotificationService(notificationRepository, notificationQueryRepository);
    }
}
