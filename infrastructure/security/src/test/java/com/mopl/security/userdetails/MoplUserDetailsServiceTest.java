package com.mopl.security.userdetails;

import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MoplUserDetailsService 단위 테스트")
class MoplUserDetailsServiceTest {

    @Mock
    private UserService userService;

    private MoplUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new MoplUserDetailsService(userService);
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsernameTest {

        @Test
        @DisplayName("존재하는 이메일로 사용자를 조회한다")
        void withExistingEmail_loadsUser() {
            // given
            String email = "test@example.com";
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email(email)
                .name("Test User")
                .password("encoded-password")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            given(userService.getByEmail(email)).willReturn(user);

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // then
            assertThat(userDetails).isInstanceOf(MoplUserDetails.class);
            MoplUserDetails moplUserDetails = (MoplUserDetails) userDetails;
            assertThat(moplUserDetails.userId()).isEqualTo(userId);
            assertThat(moplUserDetails.email()).isEqualTo(email);
            assertThat(moplUserDetails.getPassword()).isEqualTo("encoded-password");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 UsernameNotFoundException이 발생한다")
        void withNonExistingEmail_throwsException() {
            // given
            String email = "nonexistent@example.com";
            given(userService.getByEmail(email)).willThrow(UserNotFoundException.withEmail(email));

            // when & then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("ADMIN 역할 사용자를 조회한다")
        void withAdminRole_loadsUser() {
            // given
            String email = "admin@example.com";
            UUID userId = UUID.randomUUID();
            UserModel adminUser = UserModel.builder()
                .id(userId)
                .email(email)
                .name("Admin User")
                .password("admin-password")
                .role(UserModel.Role.ADMIN)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            given(userService.getByEmail(email)).willReturn(adminUser);

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // then
            MoplUserDetails moplUserDetails = (MoplUserDetails) userDetails;
            assertThat(moplUserDetails.role()).isEqualTo(UserModel.Role.ADMIN);
            assertThat(moplUserDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("잠긴 계정의 사용자를 조회한다")
        void withLockedAccount_loadsUserWithLockedStatus() {
            // given
            String email = "locked@example.com";
            UUID userId = UUID.randomUUID();
            UserModel lockedUser = UserModel.builder()
                .id(userId)
                .email(email)
                .name("Locked User")
                .password("password")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .createdAt(Instant.now())
                .locked(true)
                .build();

            given(userService.getByEmail(email)).willReturn(lockedUser);

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // then
            MoplUserDetails moplUserDetails = (MoplUserDetails) userDetails;
            assertThat(moplUserDetails.locked()).isTrue();
            assertThat(moplUserDetails.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("OAuth2 사용자를 조회한다")
        void withOAuth2User_loadsUser() {
            // given
            String email = "oauth@example.com";
            UUID userId = UUID.randomUUID();
            UserModel oauthUser = UserModel.builder()
                .id(userId)
                .email(email)
                .name("OAuth User")
                .password("{oauth2}")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            given(userService.getByEmail(email)).willReturn(oauthUser);

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // then
            MoplUserDetails moplUserDetails = (MoplUserDetails) userDetails;
            assertThat(moplUserDetails.userId()).isEqualTo(userId);
            assertThat(moplUserDetails.getPassword()).isEqualTo("{oauth2}");
        }
    }
}
