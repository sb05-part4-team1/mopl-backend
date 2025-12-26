package com.mopl.api.application.user;

import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserInfo toInfo(UserModel userModel) {
        return new UserInfo(
            userModel.getId(),
            userModel.getCreatedAt(),
            userModel.getEmail(),
            userModel.getName(),
            userModel.getProfileImageUrl(),
            userModel.getRole(),
            userModel.isLocked()
        );
    }
}
