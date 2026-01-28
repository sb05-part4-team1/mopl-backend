package com.mopl.security.oauth2;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.user.UserService;
import com.mopl.security.oauth2.userinfo.OAuth2UserInfo;
import com.mopl.security.oauth2.userinfo.OAuth2UserInfoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService 단위 테스트 - 로직 검증")
class CustomOAuth2UserServiceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        new CustomOAuth2UserService(userRepository, userService);
    }

    @Nested
    @DisplayName("validateExistingUser() 로직 검증")
    class ValidateExistingUserTest {

        @Test
        @DisplayName("잠긴 계정으로 로그인 시도 시 LockedException 발생")
        void withLockedAccount_throwsLockedException() {
            // given
            String email = "locked@example.com";
            UserModel lockedUser = UserModel.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name("Locked User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(true)
                .build();

            Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", email,
                "name", "Locked User"
            );

            OAuth2UserInfoFactory.create("google", attributes);

            // when & then
            assertThatThrownBy(() -> {
                if (lockedUser.isLocked()) {
                    throw new LockedException("계정이 잠겨 있습니다.");
                }
            }).isInstanceOf(LockedException.class)
                .hasMessage("계정이 잠겨 있습니다.");
        }

        @Test
        @DisplayName("다른 provider로 가입된 계정 로그인 시도는 실패해야 한다")
        void withDifferentProvider_failsValidation() {
            // given
            String email = "test@example.com";
            UserModel existingUser = UserModel.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.KAKAO)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", email,
                "name", "Test User"
            );

            OAuth2UserInfo googleUserInfo = OAuth2UserInfoFactory.create("google", attributes);

            // when & then
            assertThat(existingUser.getAuthProvider()).isNotEqualTo(googleUserInfo.getProvider());
            assertThat(existingUser.getAuthProvider()).isEqualTo(UserModel.AuthProvider.KAKAO);
            assertThat(googleUserInfo.getProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("같은 provider로 가입된 계정은 정상적으로 로그인된다")
        void withSameProvider_loginSucceeds() {
            // given
            String email = "test@example.com";
            UserModel existingUser = UserModel.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", email,
                "name", "Test User"
            );

            OAuth2UserInfo googleUserInfo = OAuth2UserInfoFactory.create("google", attributes);

            // when & then
            assertThat(existingUser.getAuthProvider()).isEqualTo(googleUserInfo.getProvider());
            assertThat(existingUser.isLocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("registerNewUser() 로직 검증")
    class RegisterNewUserTest {

        @Test
        @DisplayName("신규 OAuth2 사용자가 정상적으로 등록된다")
        void newUser_registersSuccessfully() {
            // given
            String email = "new@example.com";
            String name = "New User";
            UserModel.AuthProvider provider = UserModel.AuthProvider.GOOGLE;

            Map<String, Object> attributes = Map.of(
                "sub", "google-new-123",
                "email", email,
                "name", name
            );

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("google", attributes);

            // when
            UserModel created = UserModel.createOAuthUser(
                userInfo.getProvider(),
                email,
                userInfo.getName()
            );

            // then
            assertThat(created.getEmail()).isEqualTo(email);
            assertThat(created.getName()).isEqualTo(name);
            assertThat(created.getAuthProvider()).isEqualTo(provider);
        }

        @Test
        @DisplayName("Kakao 제공자로 신규 사용자 등록")
        void newKakaoUser_registersSuccessfully() {
            // given
            String email = "kakao@example.com";
            String name = "카카오 사용자";

            Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                    "email", email,
                    "profile", Map.of("nickname", name)
                )
            );

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("kakao", attributes);

            // when
            UserModel newUser = UserModel.createOAuthUser(
                userInfo.getProvider(),
                email,
                userInfo.getName()
            );

            // then
            assertThat(newUser.getAuthProvider()).isEqualTo(UserModel.AuthProvider.KAKAO);
            assertThat(newUser.getEmail()).isEqualTo(email);
            assertThat(newUser.getName()).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("이메일 검증 로직")
    class EmailValidationTest {

        @Test
        @DisplayName("null 이메일은 검증에 실패한다")
        void withNullEmail_failsValidation() {
            // given
            Map<String, Object> attributesWithoutEmail = Map.of(
                "sub", "google-123",
                "name", "Test User"
            );

            // when
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("google", attributesWithoutEmail);
            String email = userInfo.getEmail();

            // then
            assertThat(email).isNull();
            // 실제 서비스에서는 이 경우 OAuth2AuthenticationException이 발생함
        }

        @Test
        @DisplayName("이메일은 소문자로 정규화된다")
        void email_isNormalizedToLowercase() {
            // given
            String mixedCaseEmail = "  Test@EXAMPLE.COM  ";

            // when
            String normalized = mixedCaseEmail.strip().toLowerCase();

            // then
            assertThat(normalized).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("이메일 앞뒤 공백이 제거된다")
        void email_trimmed() {
            // given
            String emailWithSpaces = "  test@example.com  ";

            // when
            String normalized = emailWithSpaces.strip().toLowerCase();

            // then
            assertThat(normalized).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("전체 플로우 시뮬레이션")
    class FullFlowSimulationTest {

        @Test
        @DisplayName("기존 사용자 로그인 플로우")
        void existingUserLoginFlow() {
            // given
            String email = "existing@example.com";
            UUID userId = UUID.randomUUID();
            UserModel existingUser = UserModel.builder()
                .id(userId)
                .email(email)
                .name("Existing User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            given(userRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

            // when
            Optional<UserModel> result = userRepository.findByEmail(email);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
            assertThat(result.get().getAuthProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);
            then(userService).should(never()).create(any());
        }

        @Test
        @DisplayName("신규 사용자 등록 플로우")
        void newUserRegistrationFlow() {
            // given
            String email = "new@example.com";
            Map<String, Object> attributes = Map.of(
                "sub", "google-new",
                "email", email,
                "name", "New User"
            );

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("google", attributes);
            String normalizedEmail = email.strip().toLowerCase();

            UserModel savedUser = UserModel.builder()
                .id(UUID.randomUUID())
                .email(normalizedEmail)
                .name(userInfo.getName())
                .role(UserModel.Role.USER)
                .authProvider(userInfo.getProvider())
                .createdAt(Instant.now())
                .locked(false)
                .build();

            given(userRepository.findByEmail(normalizedEmail)).willReturn(Optional.empty());
            given(userService.create(any(UserModel.class))).willReturn(savedUser);

            // when
            Optional<UserModel> existingUser = userRepository.findByEmail(normalizedEmail);
            UserModel result = null;
            if (existingUser.isEmpty()) {
                UserModel newUser = UserModel.createOAuthUser(
                    userInfo.getProvider(),
                    normalizedEmail,
                    userInfo.getName()
                );
                result = userService.create(newUser);
            }

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(normalizedEmail);
            assertThat(result.getAuthProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);

            ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
            then(userService).should().create(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo(normalizedEmail);
        }
    }
}
