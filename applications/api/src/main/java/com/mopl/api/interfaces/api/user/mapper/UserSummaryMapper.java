package com.mopl.api.interfaces.api.user.mapper;

import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.domain.model.user.UserModel;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSummaryMapper {

    private final StorageProvider storageProvider;

    public UserSummary toSummary(UserModel userModel) {
        if (userModel == null) {
            return null;
        }

        return new UserSummary(
            userModel.getId(),
            userModel.getName(),
            storageProvider.getUrl(userModel.getProfileImagePath())
        );
    }
}
