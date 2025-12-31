package com.mopl.api.config;

import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserRoleUpdateRequest;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.model.user.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mopl.admin", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AdminProperties.class)
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final AdminProperties adminProperties;

    private final UserFacade userFacade;

    @Override
    public void run(ApplicationArguments args) {
        UserCreateRequest request = new UserCreateRequest(
            adminProperties.email(),
            adminProperties.name(),
            adminProperties.password()
        );

        try {
            UserModel user = userFacade.signUp(request);
            UserModel admin = userFacade.updateRoleInternal(
                new UserRoleUpdateRequest(UserModel.Role.ADMIN),
                user.getId()
            );
            log.info("관리자 계정이 생성되었습니다: email={}", admin.getEmail());
        } catch (DuplicateEmailException e) {
            log.debug("관리자 계정이 이미 존재합니다: email={}", adminProperties.email());
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 예상치 못한 오류가 발생했습니다.", e);
        }
    }
}
