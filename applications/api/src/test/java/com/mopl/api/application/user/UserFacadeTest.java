package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacade 단위 테스트")
class UserFacadeTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserFacade userFacade;

    @Nested
    @DisplayName("signUp()")
    class SignUpTest {

        @Test
        @DisplayName("유효한 요청 시 회원가입 성공")
        void withValidRequest_signUpSuccess() {
            // given
            String email = "test@example.com";
            String name = "test";
            String password = "P@ssw0rd!";
            UserCreateRequest request = new UserCreateRequest(email, name, password);

            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            UserModel savedUserModel = UserModel.builder()
                .id(userId)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password(password)
                .profileImageUrl(null)
                .role(Role.USER)
                .locked(false)
                .build();

            given(userService.create(any(UserModel.class))).willReturn(savedUserModel);

            // when
            UserModel result = userFacade.signUp(request);

            // then
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getRole()).isEqualTo(Role.USER);
            assertThat(result.isLocked()).isFalse();

            then(userService).should().create(any(UserModel.class));
        }

        @Test
        @DisplayName("이메일과 이름의 공백이 제거되고 이메일이 소문자로 처리된다")
        void withWhitespace_shouldTrimAndLowercase() {
            // given
            String email = "  TEST@EXAMPLE.COM  ";
            String name = "  test  ";
            String password = "P@ssw0rd!";
            UserCreateRequest request = new UserCreateRequest(email, name, password);

            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            String expectedEmail = "test@example.com";
            String expectedName = "test";

            UserModel savedUserModel = UserModel.builder()
                .id(userId)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(AuthProvider.EMAIL)
                .email(expectedEmail)
                .name(expectedName)
                .password(password)
                .profileImageUrl(null)
                .role(Role.USER)
                .locked(false)
                .build();

            given(userService.create(any(UserModel.class))).willReturn(savedUserModel);

            // when
            UserModel result = userFacade.signUp(request);

            // then
            assertThat(result.getEmail()).isEqualTo(expectedEmail);
            assertThat(result.getName()).isEqualTo(expectedName);
        }
    }
}
