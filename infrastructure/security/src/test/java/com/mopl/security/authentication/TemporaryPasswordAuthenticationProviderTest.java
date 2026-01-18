package com.mopl.security.authentication;

import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemporaryPasswordAuthenticationProvider 단위 테스트")
class TemporaryPasswordAuthenticationProviderTest {

    @Mock
    private TemporaryPasswordRepository temporaryPasswordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private TemporaryPasswordAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TemporaryPasswordAuthenticationProvider(temporaryPasswordRepository,
            passwordEncoder);
    }

    @Nested
    @DisplayName("additionalAuthenticationChecks()")
    class AdditionalAuthenticationChecksTest {

        @Test
        @DisplayName("credentials가 null이면 BadCredentialsException 발생")
        void withNullCredentials_throwsBadCredentialsException() {
            // given
            MoplUserDetails userDetails = mock(MoplUserDetails.class);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com", null);

            // when & then
            assertThatThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                authentication))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 입력되지 않았습니다");
        }

        @Test
        @DisplayName("기존 비밀번호가 일치하면 인증 성공")
        void withValidMainPassword_authenticationSucceeds() {
            // given
            String email = "user@example.com";
            String rawPassword = "correctPassword";
            String encodedPassword = "encodedCorrectPassword";

            MoplUserDetails userDetails = createMoplUserDetails(email, encodedPassword);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, rawPassword);

            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                    authentication));
        }

        @Test
        @DisplayName("기존 비밀번호는 틀리지만 임시 비밀번호가 일치하면 인증 성공")
        void withInvalidMainPasswordButValidTempPassword_authenticationSucceeds() {
            // given
            String email = "user@example.com";
            String rawPassword = "tempPassword123";
            String storedPassword = "encodedMainPassword";
            String encodedTempPassword = "encodedTempPassword";

            MoplUserDetails userDetails = createMoplUserDetails(email, storedPassword);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, rawPassword);

            given(passwordEncoder.matches(rawPassword, storedPassword)).willReturn(false);
            given(temporaryPasswordRepository.findByEmail(email)).willReturn(Optional.of(
                encodedTempPassword));
            given(passwordEncoder.matches(rawPassword, encodedTempPassword)).willReturn(true);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                    authentication));
        }

        @Test
        @DisplayName("기존 비밀번호와 임시 비밀번호 모두 틀리면 BadCredentialsException 발생")
        void withBothPasswordsInvalid_throwsBadCredentialsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "wrongPassword";
            String storedPassword = "encodedMainPassword";
            String encodedTempPassword = "encodedTempPassword";

            MoplUserDetails userDetails = createMoplUserDetails(email, storedPassword);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, rawPassword);

            given(passwordEncoder.matches(rawPassword, storedPassword)).willReturn(false);
            given(temporaryPasswordRepository.findByEmail(email)).willReturn(Optional.of(
                encodedTempPassword));
            given(passwordEncoder.matches(rawPassword, encodedTempPassword)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                authentication))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("임시 비밀번호가 없고 기존 비밀번호도 틀리면 BadCredentialsException 발생")
        void withNoTempPasswordAndInvalidMainPassword_throwsBadCredentialsException() {
            // given
            String email = "user@example.com";
            String rawPassword = "wrongPassword";
            String storedPassword = "encodedMainPassword";

            MoplUserDetails userDetails = createMoplUserDetails(email, storedPassword);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, rawPassword);

            given(passwordEncoder.matches(rawPassword, storedPassword)).willReturn(false);
            given(temporaryPasswordRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                authentication))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("MoplUserDetails가 아닌 경우 getUsername()으로 이메일을 가져온다")
        void withNonMoplUserDetails_usesGetUsername() {
            // given
            String email = "user@example.com";
            String rawPassword = "correctPassword";
            String storedPassword = "encodedPassword";

            UserDetails userDetails = User.builder()
                .username(email)
                .password(storedPassword)
                .authorities(Collections.emptyList())
                .build();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email, rawPassword);

            given(passwordEncoder.matches(rawPassword, storedPassword)).willReturn(true);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> provider.additionalAuthenticationChecks(userDetails,
                    authentication));
        }
    }

    private MoplUserDetails createMoplUserDetails(String email, String password) {
        MoplUserDetails userDetails = mock(MoplUserDetails.class);
        given(userDetails.email()).willReturn(email);
        given(userDetails.getPassword()).willReturn(password);
        return userDetails;
    }
}
