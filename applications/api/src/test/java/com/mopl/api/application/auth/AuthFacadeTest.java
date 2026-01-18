package com.mopl.api.application.auth;

import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.domain.service.user.UserService;
import com.mopl.mail.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacade 단위 테스트")
class AuthFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private TemporaryPasswordRepository temporaryPasswordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthFacade authFacade;

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTest {

        @Test
        @DisplayName("유효한 이메일로 비밀번호 초기화 요청 시 임시 비밀번호 발송 성공")
        void withValidEmail_sendsTemporaryPassword() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedTempPassword";
            UserModel userModel = UserModelFixture.builder()
                .set("email", email)
                .sample();

            given(userService.getByEmail(email)).willReturn(userModel);
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

            // when
            authFacade.resetPassword(email);

            // then
            then(userService).should().getByEmail(email);
            then(temporaryPasswordRepository).should().save(eq(email), eq(encodedPassword));
            then(emailService).should().sendTemporaryPassword(eq(email), anyString(), any(
                LocalDateTime.class));
        }

        @Test
        @DisplayName("임시 비밀번호가 12자로 생성된다")
        void generatesPasswordWith12Characters() {
            // given
            String email = "test@example.com";
            UserModel userModel = UserModelFixture.builder()
                .set("email", email)
                .sample();

            given(userService.getByEmail(email)).willReturn(userModel);
            given(passwordEncoder.encode(anyString())).willReturn("encoded");

            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            // when
            authFacade.resetPassword(email);

            // then
            then(emailService).should().sendTemporaryPassword(eq(email), passwordCaptor.capture(),
                any(LocalDateTime.class));
            assertThat(passwordCaptor.getValue()).hasSize(12);
        }

        @Test
        @DisplayName("임시 비밀번호가 허용된 문자로만 구성된다")
        void generatesPasswordWithValidCharacters() {
            // given
            String email = "test@example.com";
            String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
            UserModel userModel = UserModelFixture.builder()
                .set("email", email)
                .sample();

            given(userService.getByEmail(email)).willReturn(userModel);
            given(passwordEncoder.encode(anyString())).willReturn("encoded");

            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            // when
            authFacade.resetPassword(email);

            // then
            then(emailService).should().sendTemporaryPassword(eq(email), passwordCaptor.capture(),
                any(LocalDateTime.class));
            String generatedPassword = passwordCaptor.getValue();
            for (char c : generatedPassword.toCharArray()) {
                assertThat(allowedChars).contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 요청 시 예외 발생")
        void withNonExistentEmail_throwsException() {
            // given
            String email = "nonexistent@example.com";

            given(userService.getByEmail(email)).willThrow(UserNotFoundException.withEmail(email));

            // when & then
            assertThatThrownBy(() -> authFacade.resetPassword(email))
                .isInstanceOf(UserNotFoundException.class);

            then(temporaryPasswordRepository).should(never()).save(anyString(), anyString());
            then(emailService).should(never()).sendTemporaryPassword(anyString(), anyString(), any(
                LocalDateTime.class));
        }
    }
}
