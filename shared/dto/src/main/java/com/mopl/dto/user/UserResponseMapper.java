package com.mopl.dto.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private final StorageProvider storageProvider;

    public UserResponse toResponse(UserModel userModel) {
        return new UserResponse(
            userModel.getId(),
            userModel.getCreatedAt(),
            userModel.getEmail(),
            userModel.getName(),
            storageProvider.getUrl(userModel.getProfileImagePath()),
            userModel.getRole(),
            userModel.isLocked()
        );
    }
}
