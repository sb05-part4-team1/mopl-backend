package com.mopl.dto.user;

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
@DisplayName("UserSummaryMapper 단위 테스트")
class UserSummaryMapperTest {

    @Mock
    private StorageProvider storageProvider;

    @InjectMocks
    private UserSummaryMapper mapper;

    @Nested
    @DisplayName("toSummary()")
    class ToSummaryTest {

        @Test
        @DisplayName("UserModel을 UserSummary로 변환")
        void withUserModel_returnsUserSummary() {
            // given
            UserModel userModel = UserModelFixture.create();
            String expectedUrl = "https://cdn.example.com/" + userModel.getProfileImagePath();
            given(storageProvider.getUrl(userModel.getProfileImagePath())).willReturn(expectedUrl);

            // when
            UserSummary result = mapper.toSummary(userModel);

            // then
            assertThat(result.userId()).isEqualTo(userModel.getId());
            assertThat(result.name()).isEqualTo(userModel.getName());
            assertThat(result.profileImageUrl()).isEqualTo(expectedUrl);
        }

        @Test
        @DisplayName("null UserModel이면 null 반환")
        void withNullUserModel_returnsNull() {
            // when
            UserSummary result = mapper.toSummary(null);

            // then
            assertThat(result).isNull();
        }
    }
}
