package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel.AuthProvider;

public interface OAuth2UserInfo {

    AuthProvider getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getProfileImageUrl();
}
