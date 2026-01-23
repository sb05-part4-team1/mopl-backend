package com.mopl.sse.config;

import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
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
}
