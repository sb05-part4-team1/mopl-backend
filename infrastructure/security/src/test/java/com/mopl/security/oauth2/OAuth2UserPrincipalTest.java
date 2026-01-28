package com.mopl.security.oauth2;

import com.mopl.domain.model.user.UserModel;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2UserPrincipal 단위 테스트")
class OAuth2UserPrincipalTest {

    @Nested
    @DisplayName("생성자 및 기본 동작")
    class ConstructorAndBasicOperationsTest {

        @Test
        @DisplayName("MoplUserDetails와 OAuth2 attributes로 OAuth2UserPrincipal을 생성한다")
        void createsOAuth2UserPrincipal() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of(
                "sub", "google-user-id",
                "email", "test@example.com",
                "name", "Test User"
            );

            // when
            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // then
            assertThat(principal).isNotNull();
            assertThat(principal.getUserDetails()).isEqualTo(userDetails);
            assertThat(principal.getAttributes()).isEqualTo(attributes);
        }
    }

    @Nested
    @DisplayName("getAuthorities()")
    class GetAuthoritiesTest {

        @Test
        @DisplayName("MoplUserDetails의 권한을 반환한다")
        void returnsAuthoritiesFromUserDetails() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("admin@example.com")
                .name("Admin User")
                .role(UserModel.Role.ADMIN)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of("sub", "google-admin-id");

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // when
            Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

            // then
            assertThat(authorities).isNotNull();
            assertThat(authorities).hasSize(1);
            assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("USER 역할의 권한을 반환한다")
        void returnsUserRoleAuthority() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("user@example.com")
                .name("Regular User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.KAKAO)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of("id", 12345678L);

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // when
            Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

            // then
            assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetNameTest {

        @Test
        @DisplayName("사용자 ID를 문자열로 반환한다")
        void returnsUserIdAsString() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of("sub", "provider-id");

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // when
            String name = principal.getName();

            // then
            assertThat(name).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("서로 다른 사용자는 다른 name을 반환한다")
        void differentUserReturnsDifferentName() {
            // given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            UserModel user1 = UserModel.builder()
                .id(userId1)
                .email("user1@example.com")
                .name("User 1")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            UserModel user2 = UserModel.builder()
                .id(userId2)
                .email("user2@example.com")
                .name("User 2")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.KAKAO)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails1 = MoplUserDetails.from(user1);
            MoplUserDetails userDetails2 = MoplUserDetails.from(user2);

            OAuth2UserPrincipal principal1 = new OAuth2UserPrincipal(userDetails1, Map.of());
            OAuth2UserPrincipal principal2 = new OAuth2UserPrincipal(userDetails2, Map.of());

            // when
            String name1 = principal1.getName();
            String name2 = principal2.getName();

            // then
            assertThat(name1).isNotEqualTo(name2);
            assertThat(name1).isEqualTo(userId1.toString());
            assertThat(name2).isEqualTo(userId2.toString());
        }
    }

    @Nested
    @DisplayName("getAttributes()")
    class GetAttributesTest {

        @Test
        @DisplayName("OAuth2 provider의 attributes를 반환한다")
        void returnsOAuth2Attributes() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of(
                "sub", "google-123",
                "email", "test@example.com",
                "name", "Test User",
                "picture", "https://example.com/picture.jpg"
            );

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // when
            Map<String, Object> returnedAttributes = principal.getAttributes();

            // then
            assertThat(returnedAttributes).isEqualTo(attributes);
            assertThat(returnedAttributes.get("sub")).isEqualTo("google-123");
            assertThat(returnedAttributes.get("email")).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("빈 attributes도 정상적으로 처리한다")
        void handlesEmptyAttributes() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> emptyAttributes = Map.of();

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, emptyAttributes);

            // when
            Map<String, Object> returnedAttributes = principal.getAttributes();

            // then
            assertThat(returnedAttributes).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserDetails()")
    class GetUserDetailsTest {

        @Test
        @DisplayName("저장된 MoplUserDetails를 반환한다")
        void returnsStoredUserDetails() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel user = UserModel.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .role(UserModel.Role.USER)
                .authProvider(UserModel.AuthProvider.GOOGLE)
                .createdAt(Instant.now())
                .locked(false)
                .build();

            MoplUserDetails userDetails = MoplUserDetails.from(user);
            Map<String, Object> attributes = Map.of("sub", "google-id");

            OAuth2UserPrincipal principal = new OAuth2UserPrincipal(userDetails, attributes);

            // when
            MoplUserDetails returnedUserDetails = principal.getUserDetails();

            // then
            assertThat(returnedUserDetails).isEqualTo(userDetails);
            assertThat(returnedUserDetails.userId()).isEqualTo(userId);
            assertThat(returnedUserDetails.email()).isEqualTo("test@example.com");
        }
    }
}
