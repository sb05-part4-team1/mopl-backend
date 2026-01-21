package com.mopl.security.oauth2.userinfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OAuth2UserInfoFactory 단위 테스트")
class OAuth2UserInfoFactoryTest {

    @Test
    @DisplayName("google registrationId로 GoogleOAuth2UserInfo 생성")
    void withGoogle_createsGoogleUserInfo() {
        // given
        Map<String, Object> attributes = Map.of(
            "sub", "google-id-123",
            "email", "test@gmail.com",
            "name", "Test User"
        );

        // when
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("google", attributes);

        // then
        assertThat(userInfo).isInstanceOf(GoogleOAuth2UserInfo.class);
        assertThat(userInfo.getEmail()).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("kakao registrationId로 KakaoOAuth2UserInfo 생성")
    void withKakao_createsKakaoUserInfo() {
        // given
        Map<String, Object> attributes = Map.of(
            "id", 123456789L,
            "kakao_account", Map.of(
                "email", "test@kakao.com",
                "profile", Map.of("nickname", "테스트")
            )
        );

        // when
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("kakao", attributes);

        // then
        assertThat(userInfo).isInstanceOf(KakaoOAuth2UserInfo.class);
        assertThat(userInfo.getEmail()).isEqualTo("test@kakao.com");
    }

    @Test
    @DisplayName("대소문자 구분 없이 registrationId 처리")
    void withUpperCase_handlesCorrectly() {
        // given
        Map<String, Object> attributes = Map.of(
            "id", 123456789L,
            "kakao_account", Map.of(
                "email", "test@kakao.com",
                "profile", Map.of("nickname", "테스트")
            )
        );

        // when
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create("KAKAO", attributes);

        // then
        assertThat(userInfo).isInstanceOf(KakaoOAuth2UserInfo.class);
    }

    @Test
    @DisplayName("email provider로 생성 시 예외 발생")
    void withEmail_throwsException() {
        // given
        Map<String, Object> attributes = Map.of();

        // when & then
        assertThatThrownBy(() -> OAuth2UserInfoFactory.create("email", attributes))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("EMAIL is not an OAuth2 provider");
    }

    @Test
    @DisplayName("지원하지 않는 provider로 생성 시 예외 발생")
    void withUnsupportedProvider_throwsException() {
        // given
        Map<String, Object> attributes = Map.of();

        // when & then
        assertThatThrownBy(() -> OAuth2UserInfoFactory.create("facebook", attributes))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
