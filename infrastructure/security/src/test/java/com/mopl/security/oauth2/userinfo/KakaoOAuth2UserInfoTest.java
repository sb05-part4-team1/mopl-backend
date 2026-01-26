package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KakaoOAuth2UserInfo 단위 테스트")
class KakaoOAuth2UserInfoTest {

    @Nested
    @DisplayName("Kakao API 응답 파싱")
    class ParseKakaoResponseTest {

        @Test
        @DisplayName("정상적인 Kakao 응답에서 사용자 정보 추출")
        void withValidResponse_extractsUserInfo() {
            // given
            Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                    "email", "test@kakao.com",
                    "profile", Map.of(
                        "nickname", "홍길동",
                        "profile_image_url", "https://k.kakaocdn.net/image.jpg"
                    )
                )
            );

            // when
            KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProvider()).isEqualTo(UserModel.AuthProvider.KAKAO);
            assertThat(userInfo.getProviderId()).isEqualTo("123456789");
            assertThat(userInfo.getEmail()).isEqualTo("test@kakao.com");
            assertThat(userInfo.getName()).isEqualTo("홍길동");
            assertThat(userInfo.getProfileImageUrl()).isEqualTo("https://k.kakaocdn.net/image.jpg");
        }

        @Test
        @DisplayName("프로필 이미지가 없는 경우 null 반환")
        void withoutProfileImage_returnsNull() {
            // given
            Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                    "email", "test@kakao.com",
                    "profile", Map.of(
                        "nickname", "홍길동"
                    )
                )
            );

            // when
            KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProfileImageUrl()).isNull();
        }

        @Test
        @DisplayName("kakao_account가 없는 경우에도 예외 없이 처리")
        void withoutKakaoAccount_handlesGracefully() {
            // given
            Map<String, Object> attributes = Map.of(
                "id", 123456789L
            );

            // when
            KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProviderId()).isEqualTo("123456789");
            assertThat(userInfo.getEmail()).isNull();
            assertThat(userInfo.getName()).isNull();
        }

        @Test
        @DisplayName("id가 Long 타입인 경우 String으로 변환")
        void withLongId_convertsToString() {
            // given
            Map<String, Object> attributes = Map.of(
                "id", 9876543210L,
                "kakao_account", Map.of(
                    "email", "test@kakao.com",
                    "profile", Map.of("nickname", "테스트")
                )
            );

            // when
            KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

            // then
            assertThat(userInfo.getProviderId()).isEqualTo("9876543210");
        }
    }
}
