package com.mopl.api.config;

import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.dto.UserCreateRequest;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInitializer 단위 테스트")
class AdminInitializerTest {

    private static final String ADMIN_EMAIL = "admin@mopl.com";
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_PASSWORD = "admin1234!";

    @Mock
    private UserFacade userFacade;

    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        AdminProperties adminProperties = new AdminProperties(
            true,
            ADMIN_EMAIL,
            ADMIN_NAME,
            ADMIN_PASSWORD
        );
        adminInitializer = new AdminInitializer(adminProperties, userFacade);
    }

    @Nested
    @DisplayName("run()")
    class RunTest {

        @Test
        @DisplayName("Admin 계정이 없으면 새로 생성하고 ADMIN 역할을 부여한다")
        void whenAdminNotExists_shouldCreateAdminAndUpdateRole() {
            // given
            UserModel createdUser = UserModelFixture.builder()
                .set("email", ADMIN_EMAIL)
                .sample();

            UserModel adminUser = UserModelFixture.builder()
                .set("email", ADMIN_EMAIL)
                .set("role", UserModel.Role.ADMIN)
                .sample();

            given(userFacade.signUp(any(UserCreateRequest.class))).willReturn(createdUser);
            given(userFacade.updateRoleInternal(eq(createdUser.getId()), eq(UserModel.Role.ADMIN)))
                .willReturn(adminUser);

            // when
            adminInitializer.run(null);

            // then
            then(userFacade).should().signUp(any(UserCreateRequest.class));
            then(userFacade).should()
                .updateRoleInternal(eq(createdUser.getId()), eq(UserModel.Role.ADMIN));
        }

        @Test
        @DisplayName("Admin 계정이 이미 존재하면 생성하지 않는다")
        void whenAdminExists_shouldNotCreateAdmin() {
            // given
            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(DuplicateEmailException.withEmail(ADMIN_EMAIL));

            // when
            adminInitializer.run(null);

            // then
            then(userFacade).should().signUp(any(UserCreateRequest.class));
            then(userFacade).should(never()).updateRoleInternal(any(), any());
        }

        @Test
        @DisplayName("예외 발생 시 에러 로그를 남기고 종료한다")
        void whenExceptionOccurs_shouldLogErrorAndContinue() {
            // given
            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(new RuntimeException("Unexpected error"));

            // when
            adminInitializer.run(null);

            // then
            then(userFacade).should().signUp(any(UserCreateRequest.class));
            then(userFacade).should(never()).updateRoleInternal(any(), any());
        }
    }
}
