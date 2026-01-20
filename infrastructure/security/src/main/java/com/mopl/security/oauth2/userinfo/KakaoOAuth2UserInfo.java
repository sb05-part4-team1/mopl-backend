package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel.AuthProvider;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.getOrDefault(
            "kakao_account",
            Map.of()
        );
        this.profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        return (String) profile.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) profile.get("profile_image_url");
    }
}
