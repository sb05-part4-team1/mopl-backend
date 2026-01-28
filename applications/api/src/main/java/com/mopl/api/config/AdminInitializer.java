package com.mopl.api.config;

import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.dto.UserCreateRequest;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "mopl.admin", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AdminProperties.class)
@RequiredArgsConstructor
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
            UserModel admin = userFacade.updateRoleInternal(user.getId(), UserModel.Role.ADMIN);
            LogContext.with("email", admin.getEmail()).info("관리자 계정이 생성되었습니다");
        } catch (DuplicateEmailException e) {
            // 이미 존재하는 경우 무시 (정상 시나리오)
        } catch (Exception e) {
            LogContext.with("email", adminProperties.email()).error("관리자 계정 생성 중 예상치 못한 오류가 발생했습니다", e);
        }
    }
}
