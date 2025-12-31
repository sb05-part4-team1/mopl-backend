package com.mopl.api.interfaces.api.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.user.UserModel.AuthProvider;
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
        @DisplayName("UserModel을 UserResponse로 변환")
        void withUserModel_returnsUserResponse() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";
            String profileImageUrl = "https://example.com/image.png";
            UserModel.Role role = UserModel.Role.USER;
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
    }
}
