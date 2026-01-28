package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoogleOAuth2UserInfo 단위 테스트")
class GoogleOAuth2UserInfoTest {

    @Nested
    @DisplayName("Google API 응답 파싱")
    class ParseGoogleResponseTest {

        @Test
        @DisplayName("정상적인 Google 응답에서 사용자 정보 추출")
        void withValidResponse_extractsUserInfo() {
            // given
            Map<String, Object> attributes = Map.of(
                "sub", "112233445566778899",
                "email", "test@gmail.com",
                "name", "홍길동",
                "picture", "https://lh3.googleusercontent.com/image.jpg"
            );

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);
            assertThat(userInfo.getProviderId()).isEqualTo("112233445566778899");
            assertThat(userInfo.getEmail()).isEqualTo("test@gmail.com");
            assertThat(userInfo.getName()).isEqualTo("홍길동");
            assertThat(userInfo.getProfileImageUrl()).isEqualTo("https://lh3.googleusercontent.com/image.jpg");
        }

        @Test
        @DisplayName("프로필 이미지가 없는 경우 null 반환")
        void withoutProfileImage_returnsNull() {
            // given
            Map<String, Object> attributes = Map.of(
                "sub", "112233445566778899",
                "email", "test@gmail.com",
                "name", "홍길동"
            );

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProfileImageUrl()).isNull();
        }

        @Test
        @DisplayName("이메일이 없는 경우 null 반환")
        void withoutEmail_returnsNull() {
            // given
            Map<String, Object> attributes = Map.of(
                "sub", "112233445566778899",
                "name", "홍길동"
            );

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getEmail()).isNull();
        }

        @Test
        @DisplayName("이름이 없는 경우 null 반환")
        void withoutName_returnsNull() {
            // given
            Map<String, Object> attributes = Map.of(
                "sub", "112233445566778899",
                "email", "test@gmail.com"
            );

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getName()).isNull();
        }

        @Test
        @DisplayName("sub가 없는 경우 null 반환")
        void withoutSub_returnsNull() {
            // given
            Map<String, Object> attributes = Map.of(
                "email", "test@gmail.com",
                "name", "홍길동"
            );

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProviderId()).isNull();
        }

        @Test
        @DisplayName("provider는 항상 GOOGLE을 반환한다")
        void getProvider_alwaysReturnsGoogle() {
            // given
            Map<String, Object> attributes = Map.of("sub", "123");

            // when
            GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProvider()).isEqualTo(UserModel.AuthProvider.GOOGLE);
        }
    }
}
