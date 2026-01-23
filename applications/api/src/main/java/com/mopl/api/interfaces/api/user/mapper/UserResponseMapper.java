package com.mopl.api.interfaces.api.user.mapper;

import com.mopl.api.interfaces.api.user.dto.UserResponse;
import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {

    public UserResponse toResponse(UserModel userModel) {
        return new UserResponse(
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
