package com.mopl.api.interfaces.api.user.mapper;

import com.mopl.api.interfaces.api.user.dto.UserResponse;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserResponseMapper 단위 테스트")
class UserResponseMapperTest {

    @Mock
    private StorageProvider storageProvider;

    @InjectMocks
    private UserResponseMapper mapper;

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTest {

        @Test
        @DisplayName("UserModel을 UserResponse로 변환")
        void withUserModel_returnsUserResponse() {
            // given
            UserModel userModel = UserModelFixture.create();
            String expectedUrl = "https://cdn.example.com/" + userModel.getProfileImagePath();
            given(storageProvider.getUrl(userModel.getProfileImagePath())).willReturn(expectedUrl);

            // when
            UserResponse result = mapper.toResponse(userModel);

            // then
            assertThat(result.id()).isEqualTo(userModel.getId());
            assertThat(result.createdAt()).isEqualTo(userModel.getCreatedAt());
            assertThat(result.email()).isEqualTo(userModel.getEmail());
            assertThat(result.name()).isEqualTo(userModel.getName());
            assertThat(result.profileImageUrl()).isEqualTo(expectedUrl);
            assertThat(result.role()).isEqualTo(userModel.getRole());
            assertThat(result.locked()).isEqualTo(userModel.isLocked());
        }
    }
}
