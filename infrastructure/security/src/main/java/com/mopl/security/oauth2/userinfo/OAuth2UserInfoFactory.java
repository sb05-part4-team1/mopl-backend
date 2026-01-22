package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuth2UserInfoFactory {

    public static OAuth2UserInfo create(String registrationId, Map<String, Object> attributes) {
        UserModel.AuthProvider provider = UserModel.AuthProvider.valueOf(registrationId.toUpperCase());

        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case EMAIL -> throw new IllegalArgumentException("EMAIL is not an OAuth2 provider");
        };
    }
}
