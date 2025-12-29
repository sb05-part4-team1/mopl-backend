package com.mopl.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mopl.domain.repository.user.FollowRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.user.FollowService;
import com.mopl.domain.service.user.UserService;

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
}
