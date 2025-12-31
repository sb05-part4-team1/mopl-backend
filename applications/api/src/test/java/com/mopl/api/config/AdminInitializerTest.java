package com.mopl.api.config;

import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserRoleUpdateRequest;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInitializer 단위 테스트")
class AdminInitializerTest {

    private static final String ADMIN_EMAIL = "admin@mopl.com";
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_PASSWORD = "admin1234!";
    private static final UUID ADMIN_ID = UUID.randomUUID();

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
            UserModel createdUser = mock(UserModel.class);
            given(createdUser.getId()).willReturn(ADMIN_ID);

            UserModel adminUser = mock(UserModel.class);
            given(adminUser.getEmail()).willReturn(ADMIN_EMAIL);

            given(userFacade.signUp(any(UserCreateRequest.class))).willReturn(createdUser);
            given(userFacade.updateRoleInternal(any(UserRoleUpdateRequest.class), eq(ADMIN_ID)))
                .willReturn(adminUser);

            // when
            adminInitializer.run(null);

            // then
            then(userFacade).should().signUp(any(UserCreateRequest.class));
            then(userFacade).should().updateRoleInternal(any(UserRoleUpdateRequest.class), eq(
                ADMIN_ID));
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
            then(userFacade).should(never()).updateRoleInternal(any(UserRoleUpdateRequest.class),
                any());
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
            then(userFacade).should(never()).updateRoleInternal(any(UserRoleUpdateRequest.class),
                any());
        }
    }
}
