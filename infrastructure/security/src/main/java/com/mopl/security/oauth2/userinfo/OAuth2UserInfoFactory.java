package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel.AuthProvider;

import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo create(String registrationId, Map<String, Object> attributes) {
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> throw new IllegalArgumentException("Kakao OAuth2 is not yet supported");
            case EMAIL -> throw new IllegalArgumentException("EMAIL is not an OAuth2 provider");
        };
    }
}
