package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserResponseMapper 단위 테스트")
class UserResponseMapperTest {

    private final UserResponseMapper mapper = new UserResponseMapper();

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("UserModel을 UserResponse로 변환한다")
        void withUserModel_returnsUserResponse() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";
            String profileImageUrl = "https://example.com/image.png";
            Role role = Role.USER;
            boolean locked = false;

            UserModel userModel = UserModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password("encodedPassword")
                .profileImageUrl(profileImageUrl)
                .role(role)
                .locked(locked)
                .build();

            // when
            UserResponse result = mapper.toResponse(userModel);

            // then
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.createdAt()).isEqualTo(now);
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.profileImageUrl()).isEqualTo(profileImageUrl);
            assertThat(result.role()).isEqualTo(role);
            assertThat(result.locked()).isEqualTo(locked);
        }

        @Test
        @DisplayName("profileImageUrl이 null이어도 정상 변환된다")
        void withNullProfileImageUrl_returnsUserResponse() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            UserModel userModel = UserModel.builder()
                .id(id)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("test")
                .password("encodedPassword")
                .profileImageUrl(null)
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserResponse result = mapper.toResponse(userModel);

            // then
            assertThat(result.profileImageUrl()).isNull();
        }

        @Test
        @DisplayName("password와 authProvider는 응답에 포함되지 않는다")
        void sensitiveFieldsAreNotExposed() {
            // given
            UserModel userModel = UserModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .updatedAt(Instant.now())
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("test")
                .password("sensitivePassword")
                .profileImageUrl(null)
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserResponse result = mapper.toResponse(userModel);

            // then
            assertThat(result).hasNoNullFieldsOrPropertiesExcept("profileImageUrl");
            assertThat(result.toString()).doesNotContain("sensitivePassword");
            assertThat(result.toString()).doesNotContain("authProvider");
        }
    }
}
