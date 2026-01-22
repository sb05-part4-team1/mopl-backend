package com.mopl.security.oauth2.userinfo;

import com.mopl.domain.model.user.UserModel;

public interface OAuth2UserInfo {

    UserModel.AuthProvider getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getProfileImageUrl();
}
