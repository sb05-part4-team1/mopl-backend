package com.mopl.api.config;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.model.user.UserModel.Role;
import com.mopl.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "mopl.admin", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AdminProperties.class)
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminProperties.email())) {
            return;
        }

        try {

            userRepository.save(admin);
            log.info(">>>> [Admin Initialized] 관리자 계정이 생성되었습니다. email: {}", adminProperties.email());
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류가 발생했습니다.", e);
        }
    }
}
