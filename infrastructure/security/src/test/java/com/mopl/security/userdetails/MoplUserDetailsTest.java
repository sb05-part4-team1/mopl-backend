package com.mopl.security.userdetails;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MoplUserDetails 단위 테스트")
class MoplUserDetailsTest {

    @Nested
    @DisplayName("getUsername()")
    class GetUsernameTest {

        @Test
        @DisplayName("userId를 문자열로 반환한다")
        void returnsUserIdAsString() {
            // given
            UUID userId = UUID.randomUUID();
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(userId)
                .role(UserModel.Role.USER)
                .build();

            // when
            String username = userDetails.getUsername();

            // then
            assertThat(username).isEqualTo(userId.toString());
        }
    }

    @Nested
    @DisplayName("isAccountNonLocked()")
    class IsAccountNonLockedTest {

        @Test
        @DisplayName("locked가 false이면 true를 반환한다")
        void withLockedFalse_returnsTrue() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            // when & then
            assertThat(userDetails.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("locked가 true이면 false를 반환한다")
        void withLockedTrue_returnsFalse() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.USER)
                .locked(true)
                .build();

            // when & then
            assertThat(userDetails.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("locked가 null이면 true를 반환한다")
        void withLockedNull_returnsTrue() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.USER)
                .locked(null)
                .build();

            // when & then
            assertThat(userDetails.isAccountNonLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("from()")
    class FromTest {

        @Test
        @DisplayName("UserModel에서 MoplUserDetails를 생성한다")
        void createsFromUserModel() {
            // given
            UUID userId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .password("encoded-password")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .createdAt(createdAt)
                .profileImagePath("/images/profile.jpg")
                .locked(false)
                .build();

            // when
            MoplUserDetails userDetails = MoplUserDetails.from(user);

            // then
            assertThat(userDetails.userId()).isEqualTo(userId);
            assertThat(userDetails.email()).isEqualTo("test@example.com");
            assertThat(userDetails.name()).isEqualTo("Test User");
            assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
            assertThat(userDetails.role()).isEqualTo(UserModel.Role.USER);
            assertThat(userDetails.createdAt()).isEqualTo(createdAt);
            assertThat(userDetails.profileImagePath()).isEqualTo("/images/profile.jpg");
            assertThat(userDetails.locked()).isFalse();
        }
    }

    @Nested
    @DisplayName("getAuthorities()")
    class GetAuthoritiesTest {

        @Test
        @DisplayName("USER 역할의 권한을 반환한다")
        void withUserRole_returnsUserAuthority() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.USER)
                .build();

            // when & then
            assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("ADMIN 역할의 권한을 반환한다")
        void withAdminRole_returnsAdminAuthority() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.ADMIN)
                .build();

            // when & then
            assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("getPassword()")
    class GetPasswordTest {

        @Test
        @DisplayName("password를 반환한다")
        void returnsPassword() {
            // given
            MoplUserDetails userDetails = MoplUserDetails.builder()
                .userId(UUID.randomUUID())
                .role(UserModel.Role.USER)
                .password("test-password")
                .build();

            // when & then
            assertThat(userDetails.getPassword()).isEqualTo("test-password");
        }
    }
}
