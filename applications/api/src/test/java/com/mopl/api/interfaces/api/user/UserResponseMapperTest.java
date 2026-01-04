package com.mopl.api.interfaces.api.user;

import com.mopl.domain.fixture.UserFixture;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            UserModel userModel = UserFixture.create();

            // when
            UserResponse result = mapper.toResponse(userModel);

            // then
            assertThat(result.id()).isEqualTo(userModel.getId());
            assertThat(result.createdAt()).isEqualTo(userModel.getCreatedAt());
            assertThat(result.email()).isEqualTo(userModel.getEmail());
            assertThat(result.name()).isEqualTo(userModel.getName());
            assertThat(result.profileImageUrl()).isEqualTo(userModel.getProfileImageUrl());
            assertThat(result.role()).isEqualTo(userModel.getRole());
            assertThat(result.locked()).isEqualTo(userModel.isLocked());
        }
    }
}
